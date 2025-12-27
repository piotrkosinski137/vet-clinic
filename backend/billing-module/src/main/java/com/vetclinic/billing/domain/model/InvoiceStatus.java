package com.vetclinic.billing.domain.model;

public enum InvoiceStatus {
    DRAFT,
    ISSUED,
    PAID,
    PARTIALLY_PAID,
    CANCELLED,
    OVERDUE
}
