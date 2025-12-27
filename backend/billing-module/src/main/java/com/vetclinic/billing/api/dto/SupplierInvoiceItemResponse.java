package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;

@Builder
public record SupplierInvoiceItemResponse(
        UUID id,
        UUID itemId,
        String itemName,
        String productCode,
        String productName,
        Integer quantity,
        String unit,
        BigDecimal netPrice,
        BigDecimal grossPrice,
        Integer discountPercent,
        Integer vatRate,
        BigDecimal vatAmount,
        String batchNumber,
        LocalDate expirationDate,
        String barcode,
        String pkwiu,
        Boolean matched) {}
