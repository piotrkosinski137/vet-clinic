package com.vetclinic.billing.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.billing.domain.model.BatchStatus;
import com.vetclinic.billing.domain.model.InventoryBatch;
import com.vetclinic.billing.domain.model.InventoryTransaction;
import com.vetclinic.billing.domain.model.SupplierInvoiceItem;
import com.vetclinic.billing.domain.model.TransactionType;
import com.vetclinic.billing.domain.port.InventoryBatchRepository;
import com.vetclinic.billing.domain.port.InventoryTransactionRepository;
import com.vetclinic.billing.domain.port.PriceListRepository;
import com.vetclinic.common.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryBatchService {

    private final InventoryBatchRepository batchRepository;
    private final PriceListRepository priceListRepository;
    private final InventoryTransactionRepository transactionRepository;

    /**
     * Create a batch from a supplier invoice item. If LOT number is provided, batch is COMPLETE.
     * Otherwise, batch is PENDING.
     */
    @Transactional
    public InventoryBatch createBatchFromInvoiceItem(SupplierInvoiceItem invoiceItem, UUID itemId) {
        var status =
                (invoiceItem.getBatchNumber() != null && !invoiceItem.getBatchNumber().isBlank())
                        ? BatchStatus.COMPLETE
                        : BatchStatus.PENDING;

        var batch =
                InventoryBatch.builder()
                        .itemId(itemId)
                        .lotNumber(invoiceItem.getBatchNumber())
                        .expirationDate(invoiceItem.getExpirationDate())
                        .quantity(BigDecimal.valueOf(invoiceItem.getQuantity()))
                        .unitCost(invoiceItem.getNetPrice())
                        .status(status)
                        .invoiceItemId(invoiceItem.getId())
                        .build();

        var savedBatch = batchRepository.save(batch);

        log.info(
                "Created batch for item {}: LOT={}, qty={}, status={}",
                itemId,
                invoiceItem.getBatchNumber(),
                invoiceItem.getQuantity(),
                status);

        return savedBatch;
    }

    /**
     * Consume stock using FIFO (First-Expiring-First-Out). Creates individual transactions for each
     * batch consumed.
     *
     * @param itemId Item to consume from
     * @param quantity Amount to consume
     * @param referenceId Reference (e.g., visit ID)
     * @param referenceType Reference type (e.g., "VISIT")
     * @return List of batches that were consumed from
     * @throws InsufficientStockException if not enough stock available
     */
    @Transactional
    public List<InventoryBatch> consumeStock(
            UUID itemId, BigDecimal quantity, UUID referenceId, String referenceType) {
        log.info(
                "Consuming {} units from item {} (ref: {} {})",
                quantity,
                itemId,
                referenceType,
                referenceId);

        // Get batches ordered by expiration (earliest first, nulls last) - FIFO
        var batches = batchRepository.findForFifoConsumption(itemId);

        // Calculate total available
        var totalAvailable =
                batches.stream()
                        .map(InventoryBatch::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalAvailable.compareTo(quantity) < 0) {
            var item = priceListRepository.findById(itemId).orElse(null);
            var itemName = item != null ? item.getName() : "Unknown item";
            throw new InsufficientStockException(itemId, itemName, quantity, totalAvailable);
        }

        var consumedBatches = new ArrayList<InventoryBatch>();
        var remaining = quantity;

        for (var batch : batches) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            var toConsume = batch.getQuantity().min(remaining);
            var quantityBefore = batch.getQuantity();

            batch.consumeQuantity(toConsume);
            batchRepository.save(batch);

            // Create transaction for this batch consumption
            createBatchTransaction(
                    batch,
                    TransactionType.USAGE,
                    toConsume.negate(),
                    quantityBefore,
                    batch.getQuantity(),
                    referenceId,
                    referenceType,
                    "FIFO consumption from batch LOT: " + batch.getLotNumber());

            consumedBatches.add(batch);
            remaining = remaining.subtract(toConsume);

            log.debug(
                    "Consumed {} from batch {} (LOT: {}), remaining in batch: {}",
                    toConsume,
                    batch.getId(),
                    batch.getLotNumber(),
                    batch.getQuantity());
        }

        // Update the item's aggregate stock quantity
        recalculateItemStock(itemId);

        log.info(
                "Consumed {} units from {} batches for item {}",
                quantity,
                consumedBatches.size(),
                itemId);

        return consumedBatches;
    }

    /**
     * Complete a pending batch by setting LOT number and expiration date.
     *
     * @param batchId Batch ID
     * @param lotNumber LOT number from physical package
     * @param expirationDate Expiration date from physical package
     * @return Updated batch
     */
    @Transactional
    public InventoryBatch completePendingBatch(
            UUID batchId, String lotNumber, LocalDate expirationDate) {
        var batch =
                batchRepository
                        .findById(batchId)
                        .orElseThrow(() -> new BatchNotFoundException(batchId));

        if (batch.getStatus() != BatchStatus.PENDING) {
            throw new IllegalStateException(
                    "Batch " + batchId + " is not pending, current status: " + batch.getStatus());
        }

        batch.complete(lotNumber, expirationDate);
        var savedBatch = batchRepository.save(batch);

        log.info(
                "Completed pending batch {}: LOT={}, expires={}",
                batchId,
                lotNumber,
                expirationDate);

        return savedBatch;
    }

    /**
     * Dispose a batch (e.g., expired stock). Creates an EXPIRED transaction.
     *
     * @param batchId Batch ID
     * @param quantity Quantity to dispose (may be partial)
     * @param reason Reason for disposal
     * @return Updated batch
     */
    @Transactional
    public InventoryBatch disposeBatch(UUID batchId, BigDecimal quantity, String reason) {
        var batch =
                batchRepository
                        .findById(batchId)
                        .orElseThrow(() -> new BatchNotFoundException(batchId));

        if (quantity.compareTo(batch.getQuantity()) > 0) {
            throw new IllegalArgumentException(
                    "Cannot dispose "
                            + quantity
                            + " from batch with quantity "
                            + batch.getQuantity());
        }

        var quantityBefore = batch.getQuantity();
        batch.consumeQuantity(quantity);
        batchRepository.save(batch);

        // Create EXPIRED transaction
        createBatchTransaction(
                batch,
                TransactionType.EXPIRED,
                quantity.negate(),
                quantityBefore,
                batch.getQuantity(),
                null,
                "DISPOSAL",
                reason);

        // Update item stock
        recalculateItemStock(batch.getItemId());

        log.info(
                "Disposed {} units from batch {} (LOT: {}), reason: {}",
                quantity,
                batchId,
                batch.getLotNumber(),
                reason);

        return batch;
    }

    /** Recalculate and update the aggregate stock quantity for a price list item. */
    @Transactional
    public void recalculateItemStock(UUID itemId) {
        var totalStock = batchRepository.sumQuantityByItemId(itemId);
        var item =
                priceListRepository
                        .findById(itemId)
                        .orElseThrow(() -> new PriceListItemNotFoundException(itemId));

        var previousStock = item.getStockQuantity();
        item.setStockQuantity(totalStock);
        priceListRepository.save(item);

        log.debug("Recalculated stock for item {}: {} -> {}", itemId, previousStock, totalStock);
    }

    /**
     * Get all batches for the current clinic.
     *
     * @return List of batches
     */
    public List<InventoryBatch> getBatches() {
        return batchRepository.findByClinicId(TenantContext.getCurrentClinicId());
    }

    /**
     * Get batches filtered by status.
     *
     * @param status Batch status
     * @return List of batches
     */
    public List<InventoryBatch> getBatchesByStatus(BatchStatus status) {
        return batchRepository.findByClinicIdAndStatus(TenantContext.getCurrentClinicId(), status);
    }

    /**
     * Get batches for a specific item.
     *
     * @param itemId Item ID
     * @return List of batches
     */
    public List<InventoryBatch> getBatchesByItemId(UUID itemId) {
        return batchRepository.findByItemId(itemId);
    }

    /**
     * Get batches expiring within the given number of days.
     *
     * @param days Number of days
     * @return List of expiring batches
     */
    public List<InventoryBatch> getExpiringSoon(int days) {
        return batchRepository.findExpiringSoon(TenantContext.getCurrentClinicId(), days);
    }

    /**
     * Count pending batches for badge display.
     *
     * @return Count of pending batches
     */
    public long countPendingBatches() {
        return batchRepository.countByClinicIdAndStatus(
                TenantContext.getCurrentClinicId(), BatchStatus.PENDING);
    }

    /**
     * Get batch by ID.
     *
     * @param batchId Batch ID
     * @return Batch
     */
    public InventoryBatch getBatchById(UUID batchId) {
        return batchRepository
                .findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));
    }

    private void createBatchTransaction(
            InventoryBatch batch,
            TransactionType type,
            BigDecimal quantity,
            BigDecimal quantityBefore,
            BigDecimal quantityAfter,
            UUID referenceId,
            String referenceType,
            String notes) {

        var transaction =
                InventoryTransaction.builder()
                        .itemId(batch.getItemId())
                        .transactionType(type)
                        .quantity(quantity)
                        .quantityBefore(quantityBefore)
                        .quantityAfter(quantityAfter)
                        .referenceId(referenceId)
                        .referenceType(referenceType)
                        .batchNumber(batch.getLotNumber())
                        .expirationDate(batch.getExpirationDate())
                        .unitCost(batch.getUnitCost())
                        .notes(notes)
                        .batchId(batch.getId())
                        .build();

        transactionRepository.save(transaction);
    }
}
