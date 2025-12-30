package com.vetclinic.billing.fixtures;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.vetclinic.billing.domain.model.BatchStatus;
import com.vetclinic.billing.domain.model.InventoryBatch;

/**
 * Test fixture builder for InventoryBatch entities. Provides sensible defaults and fluent API for
 * creating test data.
 */
public final class InventoryBatchFixture {

    private UUID itemId = UUID.randomUUID();
    private String lotNumber = "LOT-001";
    private LocalDate expirationDate = LocalDate.now().plusMonths(6);
    private BigDecimal quantity = BigDecimal.valueOf(100);
    private BigDecimal unitCost = BigDecimal.valueOf(10.00);
    private BatchStatus status = BatchStatus.COMPLETE;
    private UUID invoiceItemId = null;

    private InventoryBatchFixture() {}

    /** Creates a default batch fixture with sensible defaults. */
    public static InventoryBatchFixture aBatch() {
        return new InventoryBatchFixture();
    }

    /** Creates a complete batch with LOT number and expiration date. */
    public static InventoryBatchFixture aCompleteBatch() {
        return new InventoryBatchFixture()
                .withLotNumber("LOT-2024-001")
                .withExpirationDate(LocalDate.now().plusMonths(12))
                .withStatus(BatchStatus.COMPLETE);
    }

    /** Creates a pending batch without LOT number or expiration date. */
    public static InventoryBatchFixture aPendingBatch() {
        return new InventoryBatchFixture()
                .withLotNumber(null)
                .withExpirationDate(null)
                .withStatus(BatchStatus.PENDING);
    }

    /** Creates a depleted batch with zero quantity. */
    public static InventoryBatchFixture aDepletedBatch() {
        return new InventoryBatchFixture()
                .withQuantity(BigDecimal.ZERO)
                .withStatus(BatchStatus.DEPLETED);
    }

    /** Creates an expired batch with past expiration date. */
    public static InventoryBatchFixture anExpiredBatch() {
        return new InventoryBatchFixture()
                .withLotNumber("LOT-EXPIRED")
                .withExpirationDate(LocalDate.now().minusDays(30))
                .withStatus(BatchStatus.COMPLETE);
    }

    /** Creates a batch expiring within the given number of days. */
    public static InventoryBatchFixture anExpiringSoonBatch(int days) {
        return new InventoryBatchFixture()
                .withLotNumber("LOT-EXPIRING-SOON")
                .withExpirationDate(LocalDate.now().plusDays(days - 1))
                .withStatus(BatchStatus.COMPLETE);
    }

    public InventoryBatchFixture withItemId(UUID itemId) {
        this.itemId = itemId;
        return this;
    }

    public InventoryBatchFixture withLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
        return this;
    }

    public InventoryBatchFixture withExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public InventoryBatchFixture withQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        return this;
    }

    public InventoryBatchFixture withQuantity(int quantity) {
        this.quantity = BigDecimal.valueOf(quantity);
        return this;
    }

    public InventoryBatchFixture withQuantity(double quantity) {
        this.quantity = BigDecimal.valueOf(quantity);
        return this;
    }

    public InventoryBatchFixture withUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
        return this;
    }

    public InventoryBatchFixture withStatus(BatchStatus status) {
        this.status = status;
        return this;
    }

    public InventoryBatchFixture withInvoiceItemId(UUID invoiceItemId) {
        this.invoiceItemId = invoiceItemId;
        return this;
    }

    /** Builds an InventoryBatch entity with the configured values. */
    public InventoryBatch build() {
        return InventoryBatch.builder()
                .itemId(itemId)
                .lotNumber(lotNumber)
                .expirationDate(expirationDate)
                .quantity(quantity)
                .unitCost(unitCost)
                .status(status)
                .invoiceItemId(invoiceItemId)
                .build();
    }

    /** Builds an InventoryBatch entity with a pre-set ID. Useful for mocking scenarios. */
    public InventoryBatch buildWithId(UUID id) {
        var batch = build();
        setId(batch, id);
        return batch;
    }

    /** Builds an InventoryBatch entity with a random ID. Useful for mocking scenarios. */
    public InventoryBatch buildWithRandomId() {
        return buildWithId(UUID.randomUUID());
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getSuperclass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID", e);
        }
    }
}
