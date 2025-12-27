package com.vetclinic.billing.domain;

import java.util.EnumSet;
import java.util.Set;

import com.vetclinic.billing.domain.model.InvoiceStatus;

/**
 * Validator for invoice status transitions and payment eligibility. Encapsulates the business rules
 * for valid invoice status checks.
 */
public final class InvoiceStatusValidator {

    /** Invoice statuses that allow payment recording. */
    private static final Set<InvoiceStatus> PAYABLE_STATUSES =
            EnumSet.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE);

    private InvoiceStatusValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates that the invoice status allows payment. Only ISSUED, PARTIALLY_PAID, and OVERDUE
     * invoices can receive payments.
     *
     * @param status the current invoice status
     * @throws InvalidInvoiceStatusException if the status is null or does not allow payment
     */
    public static void requirePayable(InvoiceStatus status) {
        if (status == null) {
            throw new InvalidInvoiceStatusException("Invoice status must not be null", null);
        }
        if (!PAYABLE_STATUSES.contains(status)) {
            throw new InvalidInvoiceStatusException(
                    "Cannot record payment on invoice with status: " + status, status);
        }
    }

    /**
     * Checks if an invoice with the given status can receive payments.
     *
     * @param status the invoice status to check
     * @return true if the invoice can receive payments
     */
    public static boolean isPayable(InvoiceStatus status) {
        return PAYABLE_STATUSES.contains(status);
    }
}
