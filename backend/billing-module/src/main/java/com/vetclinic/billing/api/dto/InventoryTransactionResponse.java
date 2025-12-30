package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.vetclinic.billing.domain.model.TransactionType;

import lombok.Builder;

@Builder
public record InventoryTransactionResponse(
        UUID id,
        UUID itemId,
        String itemName,
        TransactionType transactionType,
        BigDecimal quantity,
        BigDecimal quantityBefore,
        BigDecimal quantityAfter,
        UUID referenceId,
        String referenceType,
        String batchNumber,
        LocalDate expirationDate,
        BigDecimal unitCost,
        String notes,
        String createdBy,
        Instant createdAt) {}
