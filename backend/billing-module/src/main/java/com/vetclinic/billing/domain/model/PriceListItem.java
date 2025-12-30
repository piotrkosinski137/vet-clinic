package com.vetclinic.billing.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.vetclinic.common.tenant.TenantAwareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "price_list_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceListItem extends TenantAwareEntity {

    @NotBlank(message = "Item name is required")
    @Size(max = 255, message = "Item name must not exceed 255 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    @NotNull(message = "Cost price is required")
    @PositiveOrZero(message = "Cost price cannot be negative")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @NotNull(message = "Sell price is required")
    @PositiveOrZero(message = "Sell price cannot be negative")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellPrice;

    @Column(length = 20)
    private String unit;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(length = 50)
    private String code;

    @PositiveOrZero(message = "Stock quantity cannot be negative")
    @Column(name = "stock_quantity", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockQuantity = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer reorderPoint = 5;

    @Column(length = 100)
    private String barcode;

    @Column(length = 100)
    private String supplierCode;

    @Column private LocalDate expirationDate;

    @Column(length = 100)
    private String batchNumber;

    public boolean isLowStock() {
        return stockQuantity != null
                && reorderPoint != null
                && stockQuantity.compareTo(BigDecimal.valueOf(reorderPoint)) <= 0;
    }

    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity.compareTo(BigDecimal.ZERO) > 0;
    }
}
