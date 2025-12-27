package com.vetclinic.billing.domain;

import static com.vetclinic.common.constants.AppConstants.DEFAULT_REORDER_POINT;
import static com.vetclinic.common.constants.AppConstants.INITIAL_STOCK_QUANTITY;

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

        SupplierInvoice invoice =
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
                PriceListItem priceListItem;

                if (item.getItemId() != null) {
                    // Item matched to existing price list item - update stock
                    priceListItem =
                            priceListRepository
                                    .findById(item.getItemId())
                                    .orElseThrow(
                                            () ->
                                                    new PriceListItemNotFoundException(
                                                            item.getItemId()));
                    log.info(
                            "Updating stock for existing item: {} ({})",
                            priceListItem.getName(),
                            priceListItem.getId());
                } else {
                    // Item not matched - create new price list item
                    priceListItem = createPriceListItem(item);
                    priceListItem = priceListRepository.save(priceListItem);
                    item.setItemId(priceListItem.getId());
                    log.info(
                            "Created new price list item: {} ({})",
                            priceListItem.getName(),
                            priceListItem.getId());
                }

                // Update stock quantity
                int quantityBefore = priceListItem.getStockQuantity();
                int quantityAfter = quantityBefore + item.getQuantity();
                priceListItem.setStockQuantity(quantityAfter);

                // Update batch and expiration if provided
                if (item.getBatchNumber() != null) {
                    priceListItem.setBatchNumber(item.getBatchNumber());
                }
                if (item.getExpirationDate() != null) {
                    priceListItem.setExpirationDate(item.getExpirationDate());
                }

                priceListRepository.save(priceListItem);

                // Create RECEIPT transaction
                createTransaction(
                        priceListItem.getId(),
                        TransactionType.RECEIPT,
                        item.getQuantity(),
                        quantityBefore,
                        quantityAfter,
                        invoiceId,
                        "SUPPLIER_INVOICE",
                        item.getBatchNumber(),
                        item.getExpirationDate(),
                        item.getNetPrice(),
                        "Receipt from supplier invoice: " + invoice.getInvoiceNumber());
            }

            // Update invoice status
            invoice.setStatus(SupplierInvoiceStatus.PROCESSED);
            invoice.setProcessedAt(LocalDateTime.now());
            SupplierInvoice processedInvoice = invoiceRepository.save(invoice);

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

    /**
     * Record usage from visit (decrease stock)
     *
     * @param visitId ID of the visit
     * @param materials List of used materials
     * @throws InsufficientStockException if there is not enough stock for any material
     */
    @Transactional
    public void recordUsage(UUID visitId, List<UsedMaterial> materials) {
        log.info("Recording usage for visit {}: {} materials", visitId, materials.size());

        // First pass: validate all materials have sufficient stock
        for (UsedMaterial material : materials) {
            var item =
                    priceListRepository
                            .findById(material.getMaterialId())
                            .orElseThrow(
                                    () ->
                                            new PriceListItemNotFoundException(
                                                    material.getMaterialId()));

            if (item.getStockQuantity() < material.getQuantity()) {
                throw new InsufficientStockException(
                        item.getId(),
                        item.getName(),
                        material.getQuantity(),
                        item.getStockQuantity());
            }
        }

        // Second pass: apply all stock changes
        for (UsedMaterial material : materials) {
            var item =
                    priceListRepository
                            .findById(material.getMaterialId())
                            .orElseThrow(
                                    () ->
                                            new PriceListItemNotFoundException(
                                                    material.getMaterialId()));

            var quantityBefore = item.getStockQuantity();
            var quantityAfter = quantityBefore - material.getQuantity();
            item.setStockQuantity(quantityAfter);
            priceListRepository.save(item);

            // Create USAGE transaction
            createTransaction(
                    item.getId(),
                    TransactionType.USAGE,
                    -material.getQuantity(),
                    quantityBefore,
                    quantityAfter,
                    visitId,
                    "VISIT",
                    null,
                    null,
                    null,
                    material.getNotes());

            log.info(
                    "Used {} x {} for visit {}, stock: {} -> {}",
                    material.getQuantity(),
                    item.getName(),
                    visitId,
                    quantityBefore,
                    quantityAfter);
        }
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
        log.info("Adjusting stock for item {}: new quantity = {}", itemId, newQuantity);

        if (newQuantity < 0) {
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
                itemId,
                TransactionType.ADJUSTMENT,
                newQuantity - quantityBefore,
                quantityBefore,
                newQuantity,
                null,
                "MANUAL",
                null,
                null,
                null,
                reason);

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
                .stockQuantity(INITIAL_STOCK_QUANTITY)
                .reorderPoint(DEFAULT_REORDER_POINT)
                .barcode(invoiceItem.getBarcode())
                .supplierCode(invoiceItem.getProductCode())
                .expirationDate(invoiceItem.getExpirationDate())
                .batchNumber(invoiceItem.getBatchNumber())
                .build();
    }

    private void createTransaction(
            UUID itemId,
            TransactionType type,
            int quantity,
            int quantityBefore,
            int quantityAfter,
            UUID referenceId,
            String referenceType,
            String batchNumber,
            java.time.LocalDate expirationDate,
            java.math.BigDecimal unitCost,
            String notes) {

        PriceListItem item =
                priceListRepository
                        .findById(itemId)
                        .orElseThrow(() -> new PriceListItemNotFoundException(itemId));

        InventoryTransaction transaction =
                InventoryTransaction.builder()
                        .itemId(itemId)
                        .transactionType(type)
                        .quantity(quantity)
                        .quantityBefore(quantityBefore)
                        .quantityAfter(quantityAfter)
                        .referenceId(referenceId)
                        .referenceType(referenceType)
                        .batchNumber(batchNumber)
                        .expirationDate(expirationDate)
                        .unitCost(unitCost)
                        .notes(notes)
                        .build();

        transactionRepository.save(transaction);
    }
}
