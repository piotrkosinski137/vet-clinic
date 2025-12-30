package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.vetclinic.billing.domain.model.BatchStatus;
import com.vetclinic.billing.domain.model.InventoryBatch;

public record InventoryBatchResponse(
        UUID id,
        UUID itemId,
        String itemName,
        String lotNumber,
        LocalDate expirationDate,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalValue,
        BatchStatus status,
        boolean isExpiringSoon,
        Long daysUntilExpiration,
        boolean isExpired,
        Instant createdAt) {

    private static final int EXPIRING_SOON_DAYS = 30;

    public static InventoryBatchResponse from(InventoryBatch batch) {
        var item = batch.getItem();
        var itemName = item != null ? item.getName() : "Unknown";

        return new InventoryBatchResponse(
                batch.getId(),
                batch.getItemId(),
                itemName,
                batch.getLotNumber(),
                batch.getExpirationDate(),
                batch.getQuantity(),
                batch.getUnitCost(),
                batch.getTotalValue(),
                batch.getStatus(),
                batch.isExpiringSoon(EXPIRING_SOON_DAYS),
                batch.getExpirationDate() != null ? batch.getDaysUntilExpiration() : null,
                batch.isExpired(),
                batch.getCreatedAt());
    }
}
