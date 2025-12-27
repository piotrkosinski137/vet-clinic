package com.vetclinic.visit.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** DTO for materials used during a visit. */
public record UsedMaterialDto(
        UUID materialId,
        @NotBlank(message = "Material name is required") String name,
        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive")
                Integer quantity,
        @NotNull(message = "Cost price is required") BigDecimal costPrice,
        @NotNull(message = "Sell price is required") BigDecimal sellPrice,
        String unit) {}
