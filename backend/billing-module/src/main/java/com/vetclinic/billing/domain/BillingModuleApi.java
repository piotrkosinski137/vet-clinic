package com.vetclinic.billing.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Public API for the Billing module.
 *
 * <p>This interface defines the methods that other modules can use to interact with the Billing
 * module. It uses simple DTOs instead of entity types to maintain loose coupling between modules.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * @Autowired
 * private BillingModuleApi billingModule;
 *
 * long pendingCount = billingModule.countPendingInvoices();
 * BigDecimal debt = billingModule.getClientDebtAmount(clientId);
 * }</pre>
 */
public interface BillingModuleApi {

    /**
     * Count pending (unpaid) invoices.
     *
     * @return number of pending invoices
     */
    long countPendingInvoices();

    /**
     * Get total debt amount for a client.
     *
     * @param clientId the client ID
     * @return total unpaid amount
     */
    BigDecimal getClientDebtAmount(UUID clientId);

    /**
     * Check if a client has outstanding debt.
     *
     * @param clientId the client ID
     * @return true if client has unpaid invoices
     */
    boolean hasOutstandingDebt(UUID clientId);

    /**
     * Get invoices for a specific client.
     *
     * @param clientId the client ID
     * @return list of invoice summaries
     */
    List<InvoiceSummary> getInvoicesForClient(UUID clientId);

    /**
     * Get invoices for a specific visit.
     *
     * @param visitId the visit ID
     * @return list of invoice summaries
     */
    List<InvoiceSummary> getInvoicesForVisit(UUID visitId);

    /**
     * Count low stock items in inventory.
     *
     * @return number of items below minimum stock level
     */
    long countLowStockItems();

    /** Invoice summary DTO for inter-module communication. */
    record InvoiceSummary(
            UUID id,
            String invoiceNumber,
            UUID clientId,
            String clientName,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            String status,
            String issueDate,
            String dueDate) {

        public BigDecimal getOutstandingAmount() {
            return totalAmount.subtract(paidAmount);
        }
    }
}
