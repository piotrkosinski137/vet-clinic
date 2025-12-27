package com.vetclinic.billing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "supplier_invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Invoice is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private SupplierInvoice invoice;

    @Column(name = "item_id")
    private UUID itemId;

    @Size(max = 50, message = "Product code must not exceed 50 characters")
    @Column(name = "product_code", length = 50)
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Integer quantity;

    @Size(max = 20, message = "Unit must not exceed 20 characters")
    @Column(length = 20)
    private String unit;

    @Column(name = "net_price", precision = 12, scale = 2)
    private BigDecimal netPrice;

    @Column(name = "gross_price", precision = 12, scale = 2)
    private BigDecimal grossPrice;

    @Column(name = "discount_percent")
    @Builder.Default
    private Integer discountPercent = 0;

    @Column(name = "vat_rate")
    @Builder.Default
    private Integer vatRate = 23;

    @Column(name = "vat_amount", precision = 12, scale = 2)
    private BigDecimal vatAmount;

    @Size(max = 50, message = "Batch number must not exceed 50 characters")
    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Size(max = 50, message = "Barcode must not exceed 50 characters")
    @Column(length = 50)
    private String barcode;

    @Size(max = 20, message = "PKWIU must not exceed 20 characters")
    @Column(length = 20)
    private String pkwiu;

    @Column(nullable = false)
    @Builder.Default
    private Boolean matched = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
