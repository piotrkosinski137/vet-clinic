package com.vetclinic.billing.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.vetclinic.common.tenant.TenantAwareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "supplier_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierInvoice extends TenantAwareEntity {

    @NotBlank(message = "Invoice number is required")
    @Size(max = 100, message = "Invoice number must not exceed 100 characters")
    @Column(name = "invoice_number", nullable = false, length = 100)
    private String invoiceNumber;

    @Size(max = 255, message = "Supplier name must not exceed 255 characters")
    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Size(max = 20, message = "Supplier NIP must not exceed 20 characters")
    @Column(name = "supplier_nip", length = 20)
    private String supplierNip;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "sale_date")
    private LocalDate saleDate;

    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;

    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "total_net", precision = 12, scale = 2)
    private BigDecimal totalNet;

    @Column(name = "total_gross", precision = 12, scale = 2)
    private BigDecimal totalGross;

    @Column(name = "total_vat", precision = 12, scale = 2)
    private BigDecimal totalVat;

    @Size(max = 255, message = "File name must not exceed 255 characters")
    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "raw_content", columnDefinition = "TEXT")
    private String rawContent;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SupplierInvoiceStatus status = SupplierInvoiceStatus.PENDING;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @OneToMany(
            mappedBy = "invoice",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<SupplierInvoiceItem> items = new ArrayList<>();

    public void addItem(SupplierInvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }

    public void removeItem(SupplierInvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
    }
}
