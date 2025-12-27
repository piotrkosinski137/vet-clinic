package com.vetclinic.billing.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

    @NotBlank(message = "Item name is required")
    @Size(max = 255, message = "Item name must not exceed 255 characters")
    @Column(name = "item_name", nullable = false)
    private String name;

    @Size(max = 500, message = "Item description must not exceed 500 characters")
    @Column(name = "item_description")
    private String description;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(name = "item_quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @PositiveOrZero(message = "Unit price cannot be negative")
    @Column(name = "item_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "Total is required")
    @PositiveOrZero(message = "Total cannot be negative")
    @Column(name = "item_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "price_list_item_id")
    private UUID priceListItemId;
}
