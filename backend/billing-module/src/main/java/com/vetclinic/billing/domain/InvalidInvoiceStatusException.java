package com.vetclinic.billing.domain;

import com.vetclinic.billing.domain.model.InvoiceStatus;

/**
 * Exception thrown when an operation is attempted on an invoice with an invalid status. For
 * example, trying to record a payment on a DRAFT or CANCELLED invoice.
 */
public class InvalidInvoiceStatusException extends RuntimeException {

    private final InvoiceStatus currentStatus;

    public InvalidInvoiceStatusException(String message, InvoiceStatus currentStatus) {
        super(message);
        this.currentStatus = currentStatus;
    }

    public InvoiceStatus getCurrentStatus() {
        return currentStatus;
    }
}
