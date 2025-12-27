package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.vetclinic.billing.domain.model.InvoiceStatus;

import lombok.Builder;

@Builder
public record InvoiceResponse(
        UUID id,
        String invoiceNumber,
        UUID clientId,
        UUID patientId,
        UUID visitId,
        LocalDate issueDate,
        LocalDate dueDate,
        InvoiceStatus status,
        List<InvoiceItemDto> items,
        BigDecimal subtotal,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        String notes,
        Instant createdAt,
        Instant updatedAt) {}
