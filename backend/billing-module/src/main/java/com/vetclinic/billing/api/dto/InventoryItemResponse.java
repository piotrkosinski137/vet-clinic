package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.vetclinic.billing.domain.model.ItemCategory;

import lombok.Builder;

@Builder
public record InventoryItemResponse(
        UUID id,
        String name,
        String description,
        ItemCategory category,
        BigDecimal costPrice,
        BigDecimal sellPrice,
        String unit,
        Boolean active,
        String code,
        BigDecimal stockQuantity,
        Integer reorderPoint,
        String barcode,
        String supplierCode,
        LocalDate expirationDate,
        String batchNumber,
        Boolean isLowStock,
        Boolean isExpired,
        Boolean isInStock,
        Instant createdAt,
        Instant updatedAt) {}
