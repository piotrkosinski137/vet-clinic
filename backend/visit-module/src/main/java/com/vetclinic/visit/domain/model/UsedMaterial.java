package com.vetclinic.visit.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A material/product used during a visit. Stores pricing snapshot at time of use for accurate
 * billing history.
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsedMaterial {

    @Column(name = "material_id")
    private UUID materialId;

    @Column(name = "material_name", nullable = false)
    private String name;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "sell_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellPrice;

    @Column(name = "unit", length = 20)
    private String unit;

    /** Calculate total cost for this material. */
    public BigDecimal getTotalCost() {
        return costPrice.multiply(quantity);
    }

    /** Calculate total sell price for this material. */
    public BigDecimal getTotalSell() {
        return sellPrice.multiply(quantity);
    }

    /** Calculate profit for this material. */
    public BigDecimal getProfit() {
        return getTotalSell().subtract(getTotalCost());
    }
}
