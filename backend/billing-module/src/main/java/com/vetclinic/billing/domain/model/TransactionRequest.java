package com.vetclinic.billing.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;

/**
 * Encapsulates parameters for creating an inventory transaction. This record replaces the previous
 * data clump of 11 parameters.
 */
@Builder
public record TransactionRequest(
        UUID itemId,
        TransactionType type,
        BigDecimal quantity,
        BigDecimal quantityBefore,
        BigDecimal quantityAfter,
        UUID referenceId,
        String referenceType,
        String batchNumber,
        LocalDate expirationDate,
        BigDecimal unitCost,
        String notes) {}
