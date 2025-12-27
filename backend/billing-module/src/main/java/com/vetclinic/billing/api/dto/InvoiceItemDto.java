package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Builder;

@Builder
public record InvoiceItemDto(
        @NotBlank(message = "Item name is required") String name,
        String description,
        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive")
                Integer quantity,
        @NotNull(message = "Unit price is required")
                @Positive(message = "Unit price must be positive")
                BigDecimal unitPrice,
        BigDecimal total,
        UUID priceListItemId) {}
