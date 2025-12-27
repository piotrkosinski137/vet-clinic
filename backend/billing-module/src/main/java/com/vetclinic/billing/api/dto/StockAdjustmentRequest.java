package com.vetclinic.billing.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Builder;

@Builder
public record StockAdjustmentRequest(
        @NotNull(message = "New quantity is required")
                @PositiveOrZero(message = "Quantity cannot be negative")
                Integer newQuantity,
        String reason) {}
