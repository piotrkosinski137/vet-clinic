package com.vetclinic.billing.domain;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.vetclinic.billing.domain.model.InvoiceStatus;
import com.vetclinic.common.exception.BusinessException;
import com.vetclinic.common.exception.ErrorCode;

/**
 * Exception thrown when an operation cannot be performed due to an invalid invoice state.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>Attempting to modify a paid invoice
 *   <li>Attempting to issue a non-draft invoice
 *   <li>Attempting to cancel a paid invoice
 *   <li>Attempting to delete a non-draft invoice
 * </ul>
 */
public class InvalidInvoiceStateException extends BusinessException {

    public InvalidInvoiceStateException(
            UUID invoiceId, InvoiceStatus currentStatus, String operation) {
        super(
                ErrorCode.INVALID_OPERATION,
                HttpStatus.CONFLICT,
                String.format(
                        "Cannot %s invoice in status %s",
                        operation, currentStatus != null ? currentStatus.name() : "null"),
                Map.of(
                        "invoiceId", invoiceId,
                        "currentStatus", currentStatus != null ? currentStatus.name() : "null",
                        "operation", operation));
    }

    public static InvalidInvoiceStateException cannotModifyPaid(UUID invoiceId) {
        return new InvalidInvoiceStateException(invoiceId, InvoiceStatus.PAID, "modify");
    }

    public static InvalidInvoiceStateException cannotAddItemsToPaid(UUID invoiceId) {
        return new InvalidInvoiceStateException(invoiceId, InvoiceStatus.PAID, "add items to");
    }

    public static InvalidInvoiceStateException cannotIssueNonDraft(
            UUID invoiceId, InvoiceStatus currentStatus) {
        return new InvalidInvoiceStateException(invoiceId, currentStatus, "issue");
    }

    public static InvalidInvoiceStateException cannotCancelPaid(UUID invoiceId) {
        return new InvalidInvoiceStateException(invoiceId, InvoiceStatus.PAID, "cancel");
    }

    public static InvalidInvoiceStateException cannotDeleteNonDraft(
            UUID invoiceId, InvoiceStatus currentStatus) {
        return new InvalidInvoiceStateException(invoiceId, currentStatus, "delete");
    }
}
