package com.vetclinic.billing.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.vetclinic.billing.domain.model.Invoice;
import com.vetclinic.billing.domain.model.InvoiceItem;
import com.vetclinic.billing.domain.model.InvoiceStatus;

/**
 * Immutable snapshot of an Invoice for audit logging. Avoids JPA entity serialization issues and
 * circular references.
 */
public record InvoiceSnapshot(
        UUID id,
        String invoiceNumber,
        UUID clientId,
        UUID patientId,
        UUID visitId,
        InvoiceStatus status,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal taxRate,
        BigDecimal total,
        String notes,
        List<InvoiceItemSnapshot> items) {

    public record InvoiceItemSnapshot(
            String name,
            String description,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal total,
            UUID priceListItemId) {
        public static InvoiceItemSnapshot from(InvoiceItem item) {
            return new InvoiceItemSnapshot(
                    item.getName(),
                    item.getDescription(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotal(),
                    item.getPriceListItemId());
        }
    }

    public static InvoiceSnapshot from(Invoice invoice) {
        return new InvoiceSnapshot(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getClientId(),
                invoice.getPatientId(),
                invoice.getVisitId(),
                invoice.getStatus(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getTaxRate(),
                invoice.getTotalAmount(),
                invoice.getNotes(),
                invoice.getItems() != null
                        ? invoice.getItems().stream()
                                .map(InvoiceItemSnapshot::from)
                                .collect(Collectors.toList())
                        : List.of());
    }
}
