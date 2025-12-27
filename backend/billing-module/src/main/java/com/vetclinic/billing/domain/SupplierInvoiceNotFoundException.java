package com.vetclinic.billing.domain;

public class SupplierInvoiceNotFoundException extends RuntimeException {

    public SupplierInvoiceNotFoundException(String message) {
        super(message);
    }
}
