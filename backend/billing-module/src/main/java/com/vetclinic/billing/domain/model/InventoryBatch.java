package com.vetclinic.billing.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.vetclinic.common.tenant.TenantAwareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBatch extends TenantAwareEntity {

    @NotNull(message = "Item is required")
    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private PriceListItem item;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity cannot be negative")
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ZERO;

    @PositiveOrZero(message = "Unit cost cannot be negative")
    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal unitCost;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BatchStatus status = BatchStatus.PENDING;

    @Column(name = "invoice_item_id")
    private UUID invoiceItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_item_id", insertable = false, updatable = false)
    private SupplierInvoiceItem invoiceItem;

    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon(int days) {
        if (expirationDate == null) {
            return false;
        }
        var futureDate = LocalDate.now().plusDays(days);
        return !expirationDate.isBefore(LocalDate.now()) && expirationDate.isBefore(futureDate);
    }

    public long getDaysUntilExpiration() {
        if (expirationDate == null) {
            return Long.MAX_VALUE;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
    }

    public BigDecimal getTotalValue() {
        if (unitCost == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitCost.multiply(quantity);
    }

    public void consumeQuantity(BigDecimal amount) {
        if (amount.compareTo(quantity) > 0) {
            throw new IllegalArgumentException(
                    "Cannot consume " + amount + " from batch with quantity " + quantity);
        }
        this.quantity = this.quantity.subtract(amount);
        if (this.quantity.compareTo(BigDecimal.ZERO) == 0) {
            this.status = BatchStatus.DEPLETED;
        }
    }

    public void complete(String lotNumber, LocalDate expirationDate) {
        this.lotNumber = lotNumber;
        this.expirationDate = expirationDate;
        this.status = BatchStatus.COMPLETE;
    }
}
