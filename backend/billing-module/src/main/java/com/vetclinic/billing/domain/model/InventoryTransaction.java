package com.vetclinic.billing.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.vetclinic.common.tenant.TenantAwareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction extends TenantAwareEntity {

    @NotNull(message = "Item ID is required")
    @Column(nullable = false)
    private UUID itemId;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityBefore;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityAfter;

    @Column private UUID referenceId;

    @Column(length = 100)
    private String referenceType;

    @Column(length = 100)
    private String batchNumber;

    @Column private LocalDate expirationDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(length = 1000)
    private String notes;

    @Column(length = 255)
    private String createdBy;

    @Column(name = "batch_id")
    private UUID batchId;
}
