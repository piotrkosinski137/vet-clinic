package com.vetclinic.billing.domain;

import static com.vetclinic.common.validation.ValidationUtils.requireValidId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.billing.domain.model.Invoice;
import com.vetclinic.billing.domain.model.InvoiceStatus;
import com.vetclinic.billing.domain.port.InvoiceRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service responsible for client debt calculations and unpaid invoice management. Handles debt
 * summary calculations and unpaid invoice queries.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientDebtService {

    private final InvoiceRepository invoiceRepository;

    /**
     * Calculate the outstanding debt for a client by aggregating all unpaid invoices.
     *
     * @param clientId the client's UUID
     * @return ClientDebtSummary with debt totals and invoice counts
     * @throws IllegalArgumentException if clientId is null
     */
    public ClientDebtSummary calculateClientDebt(UUID clientId) {
        requireValidId(clientId, "Client ID");

        var activeInvoices =
                invoiceRepository.findByClientId(clientId).stream()
                        .filter(invoice -> invoice.getStatus() != InvoiceStatus.CANCELLED)
                        .toList();

        var totalInvoiced =
                activeInvoices.stream()
                        .map(Invoice::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalPaid =
                activeInvoices.stream()
                        .map(Invoice::getPaidAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        var unpaidInvoices =
                activeInvoices.stream().filter(invoice -> !invoice.isFullyPaid()).toList();

        var overdueCount = (int) unpaidInvoices.stream().filter(this::isOverdue).count();

        return new ClientDebtSummary(
                clientId,
                totalInvoiced.subtract(totalPaid),
                totalInvoiced,
                totalPaid,
                unpaidInvoices.size(),
                overdueCount);
    }

    private boolean isOverdue(Invoice invoice) {
        return invoice.getStatus() == InvoiceStatus.OVERDUE
                || (invoice.getDueDate() != null && invoice.getDueDate().isBefore(LocalDate.now()));
    }

    /**
     * Get all unpaid invoices for a client. This includes invoices with status ISSUED,
     * PARTIALLY_PAID, or OVERDUE.
     *
     * @param clientId the client's UUID
     * @return list of unpaid invoices
     * @throws IllegalArgumentException if clientId is null
     */
    public List<Invoice> getUnpaidInvoicesForClient(UUID clientId) {
        requireValidId(clientId, "Client ID");

        return invoiceRepository.findByClientId(clientId).stream()
                .filter(invoice -> invoice.getStatus() != InvoiceStatus.CANCELLED)
                .filter(invoice -> !invoice.isFullyPaid())
                .toList();
    }

    /**
     * Record object representing a client's debt summary.
     *
     * @param clientId the client's UUID
     * @param totalOutstanding the total outstanding amount (totalInvoiced - totalPaid)
     * @param totalInvoiced the total invoiced amount (excluding cancelled invoices)
     * @param totalPaid the total paid amount
     * @param unpaidInvoiceCount the number of unpaid invoices
     * @param overdueInvoiceCount the number of overdue invoices
     */
    public record ClientDebtSummary(
            UUID clientId,
            BigDecimal totalOutstanding,
            BigDecimal totalInvoiced,
            BigDecimal totalPaid,
            int unpaidInvoiceCount,
            int overdueInvoiceCount) {}
}
