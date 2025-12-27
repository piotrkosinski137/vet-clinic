package com.vetclinic.billing.domain;

import java.util.UUID;

import com.vetclinic.common.exception.ResourceNotFoundException;

public class InvoiceNotFoundException extends ResourceNotFoundException {

    public InvoiceNotFoundException(UUID id) {
        super("Invoice", id);
    }

    public InvoiceNotFoundException(String invoiceNumber) {
        super("Invoice with number", invoiceNumber);
    }
}
