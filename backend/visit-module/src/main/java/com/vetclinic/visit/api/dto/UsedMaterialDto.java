package com.vetclinic.visit.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTO for materials used during a visit. */
public record UsedMaterialDto(
        UUID materialId,
        @NotBlank(message = "Material name is required") String name,
        @NotNull(message = "Quantity is required")
                @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
                BigDecimal quantity,
        @NotNull(message = "Cost price is required") BigDecimal costPrice,
        @NotNull(message = "Sell price is required") BigDecimal sellPrice,
        String unit) {}
