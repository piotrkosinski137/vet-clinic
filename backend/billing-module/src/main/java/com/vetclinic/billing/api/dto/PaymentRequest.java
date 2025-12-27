package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.vetclinic.billing.domain.model.PaymentMethod;

import lombok.Builder;

@Builder
public record PaymentRequest(
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive")
                BigDecimal amount,
        @NotNull(message = "Payment method is required") PaymentMethod paymentMethod,
        String transactionReference,
        String notes) {}
