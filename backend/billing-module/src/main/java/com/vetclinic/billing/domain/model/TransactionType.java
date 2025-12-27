package com.vetclinic.billing.domain.model;

public enum TransactionType {
    RECEIPT, // Added from invoice
    USAGE, // Used in visit
    ADJUSTMENT, // Manual adjustment
    EXPIRED, // Removed due to expiration
    RETURN, // Returned to supplier
    INITIAL // Initial stock entry
}
