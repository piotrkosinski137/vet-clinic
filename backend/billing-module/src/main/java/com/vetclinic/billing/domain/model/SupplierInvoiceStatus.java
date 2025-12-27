package com.vetclinic.billing.domain.model;

public enum SupplierInvoiceStatus {
    PENDING, // Uploaded but not processed
    PROCESSED, // Successfully added to inventory
    ERROR // Failed to process
}
