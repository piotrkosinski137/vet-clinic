package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;

@Builder
public record InvoiceRequest(
        @NotNull(message = "Client ID is required") UUID clientId,
        UUID patientId,
        UUID visitId,
        LocalDate issueDate,
        LocalDate dueDate,
        @Valid List<InvoiceItemDto> items,
        BigDecimal taxRate,
        String notes) {}
