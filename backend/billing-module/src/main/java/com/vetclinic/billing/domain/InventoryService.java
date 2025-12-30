package com.vetclinic.billing.domain;

import static com.vetclinic.common.constants.AppConstants.DEFAULT_REORDER_POINT;
import static com.vetclinic.common.constants.AppConstants.INITIAL_STOCK_QUANTITY;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.billing.domain.model.InventoryTransaction;
import com.vetclinic.billing.domain.model.ItemCategory;
import com.vetclinic.billing.domain.model.PriceListItem;
import com.vetclinic.billing.domain.model.SupplierInvoice;
import com.vetclinic.billing.domain.model.SupplierInvoiceItem;
import com.vetclinic.billing.domain.model.SupplierInvoiceStatus;
import com.vetclinic.billing.domain.model.TransactionRequest;
import com.vetclinic.billing.domain.model.TransactionType;
import com.vetclinic.billing.domain.model.UsedMaterial;
import com.vetclinic.billing.domain.port.InventoryTransactionRepository;
import com.vetclinic.billing.domain.port.PriceListRepository;
import com.vetclinic.billing.domain.port.SupplierInvoiceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final PriceListRepository priceListRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final SupplierInvoiceRepository invoiceRepository;
    private final InvoiceParserService parserService;
    private final InventoryBatchService batchService;

    /**
     * Upload and parse invoice (returns preview, doesn't add to stock yet)
     *
     * @param fileName File name of the uploaded invoice
     * @param content Raw content of the invoice
     * @return Created SupplierInvoice with PENDING status
     */
    @Transactional
    public SupplierInvoice uploadInvoice(String fileName, String content) {
        log.info("Uploading invoice: {}", fileName);

        // Parse the invoice
        InvoiceParserService.ParsedInvoice parsed = parserService.parseKamsoftInvoice(content);

        // Create SupplierInvoice entity
        SupplierInvoice invoice =
                SupplierInvoice.builder()
                        .invoiceNumber(parsed.invoiceNumber())
                        .supplierName(parsed.supplierName())
                        .supplierNip(parsed.supplierNip())
                        .invoiceDate(parsed.invoiceDate())
                        .saleDate(parsed.saleDate())
                        .paymentDueDate(parsed.paymentDueDate())
                        .paymentMethod(parsed.paymentMethod())
                        .fileName(fileName)
                        .rawContent(content)
                        .status(SupplierInvoiceStatus.PENDING)
                        .build();

        // Create SupplierInvoiceItem entities and try to match them
        for (InvoiceParserService.ParsedInvoiceItem parsedItem : parsed.items()) {
            SupplierInvoiceItem item = createInvoiceItem(parsedItem);

            // Try to match to existing price_list_item by barcode or name
            PriceListItem matchedItem = findMatchingPriceListItem(parsedItem);
            if (matchedItem != null) {
                item.setItemId(matchedItem.getId());
                item.setMatched(true);
                log.info(
                        "Matched item '{}' to existing price list item {}",
                        parsedItem.productName(),
                        matchedItem.getId());
            } else {
                log.info(
                        "No match found for item '{}', will create new price list item when processed",
                        parsedItem.productName());
            }

            invoice.addItem(item);
        }

        SupplierInvoice savedInvoice = invoiceRepository.save(invoice);
        log.info(
                "Invoice {} uploaded with {} items, status: {}",
                savedInvoice.getInvoiceNumber(),
                savedInvoice.getItems().size(),
                savedInvoice.getStatus());

        return savedInvoice;
    }

    /**
     * Process invoice (add items to stock)
     *
     * @param invoiceId ID of the invoice to process
     * @return Processed invoice with PROCESSED status
     */
    @Transactional
    public SupplierInvoice processInvoice(UUID invoiceId) {
        log.info("Processing invoice: {}", invoiceId);

        var invoice =
                invoiceRepository
                        .findById(invoiceId)
                        .orElseThrow(
                                () ->
                                        new SupplierInvoiceNotFoundException(
                                                "Supplier invoice not found: " + invoiceId));

        if (invoice.getStatus() == SupplierInvoiceStatus.PROCESSED) {
            log.warn("Invoice {} already processed", invoiceId);
            return invoice;
        }

        try {
            for (SupplierInvoiceItem item : invoice.getItems()) {
                processInvoiceItem(item, invoiceId, invoice.getInvoiceNumber());
            }

            invoice.setStatus(SupplierInvoiceStatus.PROCESSED);
            invoice.setProcessedAt(LocalDateTime.now());
            var processedInvoice = invoiceRepository.save(invoice);

            log.info(
                    "Invoice {} processed successfully, {} items added to stock",
                    invoice.getInvoiceNumber(),
                    invoice.getItems().size());

            return processedInvoice;

        } catch (PriceListItemNotFoundException e) {
            log.error("Price list item not found while processing invoice {}", invoiceId, e);
            invoice.setStatus(SupplierInvoiceStatus.ERROR);
            invoiceRepository.save(invoice);
            throw new InvoiceProcessingException("Failed to process invoice: " + e.getMessage(), e);
        }
    }

    /** Process a single invoice item - update or create price list item and record transaction. */
    private void processInvoiceItem(
            SupplierInvoiceItem item, UUID invoiceId, String invoiceNumber) {
        var priceListItem = resolveOrCreatePriceListItem(item);

        var quantityBefore = priceListItem.getStockQuantity();
        var quantityAfter = quantityBefore.add(BigDecimal.valueOf(item.getQuantity()));
        priceListItem.setStockQuantity(quantityAfter);

        updateBatchAndExpiration(priceListItem, item);
        priceListRepository.save(priceListItem);

        // Create inventory batch for FIFO tracking
        batchService.createBatchFromInvoiceItem(item, priceListItem.getId());

        createReceiptTransaction(
                priceListItem, item, invoiceId, invoiceNumber, quantityBefore, quantityAfter);
    }

    /** Resolve existing price list item or create a new one from invoice item. */
    private PriceListItem resolveOrCreatePriceListItem(SupplierInvoiceItem item) {
        if (item.getItemId() != null) {
            var priceListItem =
                    priceListRepository
                            .findById(item.getItemId())
                            .orElseThrow(
                                    () -> new PriceListItemNotFoundException(item.getItemId()));
            log.info(
                    "Updating stock for existing item: {} ({})",
                    priceListItem.getName(),
                    priceListItem.getId());
            return priceListItem;
        } else {
            var priceListItem = createPriceListItem(item);
            priceListItem = priceListRepository.save(priceListItem);
            item.setItemId(priceListItem.getId());
            log.info(
                    "Created new price list item: {} ({})",
                    priceListItem.getName(),
                    priceListItem.getId());
            return priceListItem;
        }
    }

    /** Update batch number and expiration date if provided. */
    private void updateBatchAndExpiration(PriceListItem priceListItem, SupplierInvoiceItem item) {
        if (item.getBatchNumber() != null) {
            priceListItem.setBatchNumber(item.getBatchNumber());
        }
        if (item.getExpirationDate() != null) {
            priceListItem.setExpirationDate(item.getExpirationDate());
        }
    }

    /** Create a RECEIPT transaction for stock addition. */
    private void createReceiptTransaction(
            PriceListItem priceListItem,
            SupplierInvoiceItem item,
            UUID invoiceId,
            String invoiceNumber,
            BigDecimal quantityBefore,
            BigDecimal quantityAfter) {
        createTransaction(
                TransactionRequest.builder()
                        .itemId(priceListItem.getId())
                        .type(TransactionType.RECEIPT)
                        .quantity(BigDecimal.valueOf(item.getQuantity()))
                        .quantityBefore(quantityBefore)
                        .quantityAfter(quantityAfter)
                        .referenceId(invoiceId)
                        .referenceType("SUPPLIER_INVOICE")
                        .batchNumber(item.getBatchNumber())
                        .expirationDate(item.getExpirationDate())
                        .unitCost(item.getNetPrice())
                        .notes("Receipt from supplier invoice: " + invoiceNumber)
                        .build());
    }

    /**
     * Record usage from visit (decrease stock using FIFO)
     *
     * @param visitId ID of the visit
     * @param materials List of used materials
     * @throws InsufficientStockException if there is not enough stock for any material
     */
    @Transactional
    public void recordUsage(UUID visitId, List<UsedMaterial> materials) {
        log.info("Recording usage for visit {}: {} materials", visitId, materials.size());

        // Use FIFO consumption from batch service
        for (UsedMaterial material : materials) {
            // Check if this item requires inventory tracking
            var item = priceListRepository.findById(material.getMaterialId()).orElse(null);
            if (item == null) {
                log.warn(
                        "Price list item not found for material {}, skipping consumption",
                        material.getMaterialId());
                continue;
            }

            // Skip non-inventory categories (services don't consume physical stock)
            if (!requiresInventoryTracking(item.getCategory())) {
                log.debug(
                        "Skipping inventory consumption for {} item: {} ({})",
                        item.getCategory(),
                        item.getName(),
                        item.getId());
                continue;
            }

            // consumeStock handles validation, FIFO logic, and transactions
            batchService.consumeStock(
                    material.getMaterialId(), material.getQuantity(), visitId, "VISIT");

            log.info(
                    "Used {} x item {} for visit {} (FIFO)",
                    material.getQuantity(),
                    material.getMaterialId(),
                    visitId);
        }
    }

    /**
     * Determines if an item category requires inventory (batch) tracking. Services and
     * consultations don't have physical inventory.
     */
    private boolean requiresInventoryTracking(ItemCategory category) {
        return switch (category) {
            case MEDICATION, PRODUCT, VACCINATION, OTHER -> true;
            case SERVICE, CONSULTATION, PROCEDURE, LAB_TEST -> false;
        };
    }

    /**
     * Manual stock adjustment
     *
     * @param itemId ID of the price list item
     * @param newQuantity New stock quantity (must be >= 0)
     * @param reason Reason for adjustment
     * @return Updated price list item
     * @throws IllegalArgumentException if newQuantity is negative
     */
    @Transactional
    public PriceListItem adjustStock(UUID itemId, int newQuantity, String reason) {
        return adjustStock(itemId, BigDecimal.valueOf(newQuantity), reason);
    }

    @Transactional
    public PriceListItem adjustStock(UUID itemId, BigDecimal newQuantity, String reason) {
        log.info("Adjusting stock for item {}: new quantity = {}", itemId, newQuantity);

        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative: " + newQuantity);
        }

        var item =
                priceListRepository
                        .findById(itemId)
                        .orElseThrow(() -> new PriceListItemNotFoundException(itemId));

        var quantityBefore = item.getStockQuantity();
        item.setStockQuantity(newQuantity);
        var savedItem = priceListRepository.save(item);

        // Create ADJUSTMENT transaction
        createTransaction(
                TransactionRequest.builder()
                        .itemId(itemId)
                        .type(TransactionType.ADJUSTMENT)
                        .quantity(newQuantity.subtract(quantityBefore))
                        .quantityBefore(quantityBefore)
                        .quantityAfter(newQuantity)
                        .referenceType("MANUAL")
                        .notes(reason)
                        .build());

        log.info(
                "Stock adjusted for item {}: {} -> {}",
                item.getName(),
                quantityBefore,
                newQuantity);

        return savedItem;
    }

    /**
     * Get items with stock info
     *
     * @param lowStockOnly If true, return only low stock items
     * @return List of price list items
     */
    public List<PriceListItem> getInventoryItems(boolean lowStockOnly) {
        if (lowStockOnly) {
            return priceListRepository.findLowStockItems();
        }
        return priceListRepository.findAll();
    }

    /**
     * Get transactions for item
     *
     * @param itemId ID of the item (optional)
     * @return List of inventory transactions
     */
    public List<InventoryTransaction> getTransactions(UUID itemId) {
        if (itemId != null) {
            return transactionRepository.findByItemId(itemId);
        }
        // If no itemId, could return all transactions for clinic
        // For now, return empty list as we need clinic context
        return List.of();
    }

    /**
     * Get all supplier invoices
     *
     * @return List of supplier invoices
     */
    public List<SupplierInvoice> getAllInvoices() {
        // This would ideally filter by clinic ID
        // For now, returning all (should be handled by tenant context)
        return invoiceRepository.findByStatus(null);
    }

    /**
     * Get supplier invoice by ID
     *
     * @param id Invoice ID
     * @return Supplier invoice
     */
    public SupplierInvoice getInvoiceById(UUID id) {
        return invoiceRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new SupplierInvoiceNotFoundException(
                                        "Supplier invoice not found: " + id));
    }

    private SupplierInvoiceItem createInvoiceItem(
            InvoiceParserService.ParsedInvoiceItem parsedItem) {
        return SupplierInvoiceItem.builder()
                .productCode(parsedItem.productCode())
                .productName(parsedItem.productName())
                .quantity(parsedItem.quantity())
                .unit(parsedItem.unit())
                .netPrice(parsedItem.netPrice())
                .grossPrice(parsedItem.grossPrice())
                .discountPercent(parsedItem.discountPercent())
                .vatRate(parsedItem.vatRate())
                .vatAmount(parsedItem.vatAmount())
                .batchNumber(parsedItem.batchNumber())
                .expirationDate(parsedItem.expirationDate())
                .barcode(parsedItem.barcode())
                .pkwiu(parsedItem.pkwiu())
                .matched(false)
                .createdAt(Instant.now())
                .build();
    }

    private PriceListItem findMatchingPriceListItem(
            InvoiceParserService.ParsedInvoiceItem parsedItem) {
        // Try to match by barcode first
        if (parsedItem.barcode() != null && !parsedItem.barcode().trim().isEmpty()) {
            Optional<PriceListItem> byBarcode =
                    priceListRepository.findByBarcode(parsedItem.barcode());
            if (byBarcode.isPresent()) {
                return byBarcode.get();
            }
        }

        // Try to match by product code
        if (parsedItem.productCode() != null && !parsedItem.productCode().trim().isEmpty()) {
            Optional<PriceListItem> byCode =
                    priceListRepository.findByCode(parsedItem.productCode());
            if (byCode.isPresent()) {
                return byCode.get();
            }
        }

        // Try to match by name (exact match)
        List<PriceListItem> byName =
                priceListRepository.findByNameContainingIgnoreCase(parsedItem.productName());
        if (!byName.isEmpty()) {
            // Return first exact match if found
            for (PriceListItem item : byName) {
                if (item.getName().equalsIgnoreCase(parsedItem.productName())) {
                    return item;
                }
            }
        }

        return null;
    }

    private PriceListItem createPriceListItem(SupplierInvoiceItem invoiceItem) {
        return PriceListItem.builder()
                .name(invoiceItem.getProductName())
                .description("Auto-created from supplier invoice")
                .category(ItemCategory.PRODUCT)
                .costPrice(invoiceItem.getNetPrice())
                .sellPrice(invoiceItem.getGrossPrice())
                .unit(invoiceItem.getUnit())
                .active(true)
                .code(invoiceItem.getProductCode())
                .stockQuantity(BigDecimal.valueOf(INITIAL_STOCK_QUANTITY))
                .reorderPoint(DEFAULT_REORDER_POINT)
                .barcode(invoiceItem.getBarcode())
                .supplierCode(invoiceItem.getProductCode())
                .expirationDate(invoiceItem.getExpirationDate())
                .batchNumber(invoiceItem.getBatchNumber())
                .build();
    }

    private void createTransaction(TransactionRequest request) {
        PriceListItem item =
                priceListRepository
                        .findById(request.itemId())
                        .orElseThrow(() -> new PriceListItemNotFoundException(request.itemId()));

        InventoryTransaction transaction =
                InventoryTransaction.builder()
                        .itemId(request.itemId())
                        .transactionType(request.type())
                        .quantity(request.quantity())
                        .quantityBefore(request.quantityBefore())
                        .quantityAfter(request.quantityAfter())
                        .referenceId(request.referenceId())
                        .referenceType(request.referenceType())
                        .batchNumber(request.batchNumber())
                        .expirationDate(request.expirationDate())
                        .unitCost(request.unitCost())
                        .notes(request.notes())
                        .build();

        transactionRepository.save(transaction);
    }
}
