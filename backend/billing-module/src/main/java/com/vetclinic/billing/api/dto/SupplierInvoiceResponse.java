package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.vetclinic.billing.domain.model.SupplierInvoiceStatus;

import lombok.Builder;

@Builder
public record SupplierInvoiceResponse(
        UUID id,
        String invoiceNumber,
        String supplierName,
        String supplierNip,
        LocalDate invoiceDate,
        LocalDate saleDate,
        LocalDate paymentDueDate,
        String paymentMethod,
        BigDecimal totalNet,
        BigDecimal totalGross,
        BigDecimal totalVat,
        String fileName,
        SupplierInvoiceStatus status,
        LocalDateTime processedAt,
        List<SupplierInvoiceItemResponse> items) {}
