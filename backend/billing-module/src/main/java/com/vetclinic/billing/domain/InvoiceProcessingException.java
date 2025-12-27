package com.vetclinic.billing.domain;

public class InvoiceProcessingException extends RuntimeException {

    public InvoiceProcessingException(String message) {
        super(message);
    }

    public InvoiceProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
