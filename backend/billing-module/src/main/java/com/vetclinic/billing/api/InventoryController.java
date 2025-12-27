package com.vetclinic.billing.api;

import static com.vetclinic.common.security.Roles.CAN_MANAGE_INVENTORY;
import static com.vetclinic.common.security.Roles.CAN_VIEW_FINANCIALS;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vetclinic.billing.api.dto.InventoryItemResponse;
import com.vetclinic.billing.api.dto.InventoryTransactionResponse;
import com.vetclinic.billing.api.dto.StockAdjustmentRequest;
import com.vetclinic.billing.api.dto.SupplierInvoiceResponse;
import com.vetclinic.billing.domain.InventoryService;
import com.vetclinic.billing.domain.model.InventoryTransaction;
import com.vetclinic.billing.domain.model.PriceListItem;
import com.vetclinic.billing.domain.model.SupplierInvoice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    /**
     * Upload a supplier invoice file
     *
     * @param file The invoice file to upload
     * @return Parsed invoice with PENDING status
     */
    @PostMapping("/invoices/upload")
    @PreAuthorize(CAN_MANAGE_INVENTORY)
    public ResponseEntity<SupplierInvoiceResponse> uploadInvoice(
            @RequestParam("file") MultipartFile file) throws IOException {
        log.info("Uploading invoice file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String content = new String(file.getBytes());
        SupplierInvoice invoice =
                inventoryService.uploadInvoice(file.getOriginalFilename(), content);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryMapper.toSupplierInvoiceResponse(invoice));
    }

    /**
     * Get all supplier invoices
     *
     * @return List of supplier invoices
     */
    @GetMapping("/invoices")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<List<SupplierInvoiceResponse>> getInvoices() {
        List<SupplierInvoice> invoices = inventoryService.getAllInvoices();
        return ResponseEntity.ok(inventoryMapper.toSupplierInvoiceResponseList(invoices));
    }

    /**
     * Get supplier invoice by ID
     *
     * @param id Invoice ID
     * @return Supplier invoice details
     */
    @GetMapping("/invoices/{id}")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<SupplierInvoiceResponse> getInvoice(@PathVariable UUID id) {
        SupplierInvoice invoice = inventoryService.getInvoiceById(id);
        return ResponseEntity.ok(inventoryMapper.toSupplierInvoiceResponse(invoice));
    }

    /**
     * Process a supplier invoice (add items to stock)
     *
     * @param id Invoice ID
     * @return Processed invoice
     */
    @PostMapping("/invoices/{id}/process")
    @PreAuthorize(CAN_MANAGE_INVENTORY)
    public ResponseEntity<SupplierInvoiceResponse> processInvoice(@PathVariable UUID id) {
        log.info("Processing invoice: {}", id);
        SupplierInvoice invoice = inventoryService.processInvoice(id);
        return ResponseEntity.ok(inventoryMapper.toSupplierInvoiceResponse(invoice));
    }

    /**
     * Get inventory stock
     *
     * @param lowStockOnly If true, return only items with low stock
     * @return List of inventory items
     */
    @GetMapping("/stock")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<List<InventoryItemResponse>> getStock(
            @RequestParam(required = false, defaultValue = "false") Boolean lowStockOnly) {
        List<PriceListItem> items = inventoryService.getInventoryItems(lowStockOnly);
        return ResponseEntity.ok(inventoryMapper.toInventoryItemResponseList(items));
    }

    /**
     * Manually adjust stock quantity for an item
     *
     * @param itemId Item ID
     * @param request Stock adjustment request
     * @return Updated inventory item
     */
    @PatchMapping("/stock/{itemId}")
    @PreAuthorize(CAN_MANAGE_INVENTORY)
    public ResponseEntity<InventoryItemResponse> adjustStock(
            @PathVariable UUID itemId, @Valid @RequestBody StockAdjustmentRequest request) {
        log.info("Adjusting stock for item {}: {}", itemId, request.newQuantity());

        PriceListItem item =
                inventoryService.adjustStock(itemId, request.newQuantity(), request.reason());
        return ResponseEntity.ok(inventoryMapper.toInventoryItemResponse(item));
    }

    /**
     * Get inventory transactions
     *
     * @param itemId Optional item ID to filter transactions
     * @return List of inventory transactions
     */
    @GetMapping("/transactions")
    @PreAuthorize(CAN_VIEW_FINANCIALS)
    public ResponseEntity<List<InventoryTransactionResponse>> getTransactions(
            @RequestParam(required = false) UUID itemId) {
        List<InventoryTransaction> transactions = inventoryService.getTransactions(itemId);

        // Enhance transaction responses with item names
        List<InventoryTransactionResponse> responses =
                inventoryMapper.toTransactionResponseList(transactions);

        return ResponseEntity.ok(responses);
    }
}
