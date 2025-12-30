package com.vetclinic.billing.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record CompleteBatchRequest(
        @NotBlank(message = "LOT number is required") String lotNumber, LocalDate expirationDate) {}
