package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.vetclinic.billing.domain.model.InvoiceStatus;

import lombok.Builder;

@Builder
public record InvoicePrintResponse(
        UUID invoiceId,
        String invoiceNumber,
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
        String generatedAt) {}
