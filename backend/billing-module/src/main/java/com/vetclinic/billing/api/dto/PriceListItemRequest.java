package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.vetclinic.billing.domain.model.ItemCategory;

import lombok.Builder;

@Builder
public record PriceListItemRequest(
        @NotBlank(message = "Name is required") String name,
        String description,
        @NotNull(message = "Category is required") ItemCategory category,
        @NotNull(message = "Cost price is required")
                @Positive(message = "Cost price must be positive")
                BigDecimal costPrice,
        @NotNull(message = "Sell price is required")
                @Positive(message = "Sell price must be positive")
                BigDecimal sellPrice,
        String unit,
        Boolean active,
        String code) {}
