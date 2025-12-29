package com.vetclinic.visit.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Immutable snapshot of a used material for JSON storage in drafts. Mirrors the fields of
 * UsedMaterial but as a simple record for JSONB serialization.
 */
public record UsedMaterialSnapshot(
        UUID materialId,
        String name,
        Integer quantity,
        BigDecimal costPrice,
        BigDecimal sellPrice,
        String unit) {

    /** Convert from embeddable UsedMaterial to snapshot. */
    public static UsedMaterialSnapshot from(UsedMaterial material) {
        return new UsedMaterialSnapshot(
                material.getMaterialId(),
                material.getName(),
                material.getQuantity(),
                material.getCostPrice(),
                material.getSellPrice(),
                material.getUnit());
    }

    /** Convert to embeddable UsedMaterial. */
    public UsedMaterial toUsedMaterial() {
        return UsedMaterial.builder()
                .materialId(materialId)
                .name(name)
                .quantity(quantity)
                .costPrice(costPrice)
                .sellPrice(sellPrice)
                .unit(unit)
                .build();
    }
}
