package com.vetclinic.billing.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
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
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends TenantAwareEntity {

    @NotBlank(message = "Invoice number is required")
    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @NotNull(message = "Client ID is required")
    @Column(nullable = false)
    private UUID clientId;

    private UUID patientId;

    private UUID visitId;

    @NotNull(message = "Issue date is required")
    @Column(nullable = false)
    private LocalDate issueDate;

    private LocalDate dueDate;

    @NotNull(message = "Invoice status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "invoice_items", joinColumns = @JoinColumn(name = "invoice_id"))
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @NotNull(message = "Subtotal is required")
    @PositiveOrZero(message = "Subtotal must be zero or positive")
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @PositiveOrZero(message = "Tax rate must be zero or positive")
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @NotNull(message = "Tax amount is required")
    @PositiveOrZero(message = "Tax amount must be zero or positive")
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @NotNull(message = "Total amount is required")
    @PositiveOrZero(message = "Total amount must be zero or positive")
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull(message = "Paid amount is required")
    @PositiveOrZero(message = "Paid amount must be zero or positive")
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(length = 1000)
    private String notes;

    public void addItem(InvoiceItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        recalculateTotals();
    }

    public void removeItem(InvoiceItem item) {
        if (items != null) {
            items.remove(item);
            recalculateTotals();
        }
    }

    public void recalculateTotals() {
        // First, calculate item totals if not set
        for (InvoiceItem item : items) {
            if (item.getTotal() == null
                    && item.getUnitPrice() != null
                    && item.getQuantity() != null) {
                item.setTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        this.subtotal =
                items.stream()
                        .map(item -> item.getTotal() != null ? item.getTotal() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        var effectiveTaxRate = this.taxRate != null ? this.taxRate : BigDecimal.ZERO;
        this.taxAmount =
                this.subtotal
                        .multiply(effectiveTaxRate)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        this.totalAmount = this.subtotal.add(this.taxAmount);
    }

    public BigDecimal getRemainingAmount() {
        return this.totalAmount.subtract(this.paidAmount);
    }

    public boolean isFullyPaid() {
        return this.paidAmount.compareTo(this.totalAmount) >= 0;
    }
}
