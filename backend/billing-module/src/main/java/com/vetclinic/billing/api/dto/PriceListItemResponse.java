package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.vetclinic.billing.domain.model.ItemCategory;

import lombok.Builder;

@Builder
public record PriceListItemResponse(
        UUID id,
        String name,
        String description,
        ItemCategory category,
        BigDecimal costPrice,
        BigDecimal sellPrice,
        String unit,
        Boolean active,
        String code,
        Instant createdAt,
        Instant updatedAt) {}
