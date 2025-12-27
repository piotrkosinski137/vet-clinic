package com.vetclinic.billing.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedInvoice {

    private String invoiceNumber;
    private String supplierName;
    private String supplierNip;
    private LocalDate invoiceDate;
    private LocalDate saleDate;
    private LocalDate paymentDueDate;
    private String paymentMethod;
    private BigDecimal totalNet;
    private BigDecimal totalGross;
    private BigDecimal totalVat;

    @Builder.Default private List<ParsedInvoiceItem> items = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedInvoiceItem {
        private String productCode;
        private String productName;
        private Integer quantity;
        private String unit;
        private BigDecimal netPrice;
        private BigDecimal grossPrice;
        private Integer discountPercent;
        private Integer vatRate;
        private BigDecimal vatAmount;
        private String batchNumber;
        private LocalDate expirationDate;
        private String barcode;
        private String pkwiu;
    }
}
