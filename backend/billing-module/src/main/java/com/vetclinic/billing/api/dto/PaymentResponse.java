package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.vetclinic.billing.domain.model.PaymentMethod;

import lombok.Builder;

@Builder
public record PaymentResponse(
        UUID id,
        UUID invoiceId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        LocalDateTime paymentDate,
        String transactionReference,
        String notes,
        Instant createdAt) {}
