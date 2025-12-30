package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DisposeBatchRequest(
        @NotNull(message = "Quantity is required")
                @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
                BigDecimal quantity,
        @NotBlank(message = "Reason is required") String reason) {}
