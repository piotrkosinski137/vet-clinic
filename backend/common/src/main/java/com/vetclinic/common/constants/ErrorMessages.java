package com.vetclinic.common.constants;

/**
 * Centralized error messages for consistent messaging across the application. Following DRY
 * principle - all error messages should be defined here.
 */
public final class ErrorMessages {

    private ErrorMessages() {
        // Utility class - prevent instantiation
    }

    // ============================================
    // Invoice / Billing Error Messages
    // ============================================

    public static final String INVOICE_ALREADY_PAID = "Cannot modify a paid invoice";
    public static final String INVOICE_ITEMS_LOCKED = "Cannot add items to a paid invoice";
    public static final String INVOICE_ONLY_DRAFT_CAN_BE_ISSUED =
            "Only draft invoices can be issued";
    public static final String INVOICE_CANNOT_CANCEL_PAID = "Cannot cancel a paid invoice";
    public static final String INVOICE_ONLY_DRAFT_CAN_BE_DELETED =
            "Only draft invoices can be deleted";
    public static final String INVOICE_NOT_FOUND = "Invoice not found with id: %s";
    public static final String INVOICE_NOT_FOUND_BY_NUMBER = "Invoice not found with number: %s";
    public static final String PAYMENT_NOT_FOUND = "Payment not found with id: %s";
    public static final String PAYMENT_AMOUNT_MUST_BE_POSITIVE = "Payment amount must be positive";
    public static final String PAYMENT_METHOD_REQUIRED = "Payment method is required";

    // ============================================
    // Patient Error Messages
    // ============================================

    public static final String PATIENT_NOT_FOUND = "Patient not found with id: %s";
    public static final String PATIENT_NAME_REQUIRED = "Patient name is required";
    public static final String PATIENT_SPECIES_REQUIRED = "Patient species is required";

    // ============================================
    // Client Error Messages
    // ============================================

    public static final String CLIENT_NOT_FOUND = "Client not found with id: %s";
    public static final String CLIENT_EMAIL_EXISTS = "Client with email '%s' already exists";
    public static final String CLIENT_EMAIL_REQUIRED = "Client email is required";

    // ============================================
    // Veterinarian Error Messages
    // ============================================

    public static final String VETERINARIAN_NOT_FOUND = "Veterinarian not found with id: %s";
    public static final String VETERINARIAN_NOT_FOUND_BY_EMAIL =
            "Veterinarian not found with email: %s";
    public static final String VETERINARIAN_EMAIL_EXISTS =
            "Veterinarian with email '%s' already exists";

    // ============================================
    // Visit / Appointment Error Messages
    // ============================================

    public static final String VISIT_NOT_FOUND = "Visit not found with id: %s";
    public static final String VISIT_CONFLICT = "Veterinarian %s already has an appointment at %s";

    // ============================================
    // Keycloak / Authentication Error Messages
    // ============================================

    public static final String KEYCLOAK_USER_EXISTS = "User with email %s already exists";
    public static final String KEYCLOAK_USER_CREATION_FAILED =
            "Failed to create user in Keycloak. Status: %d";
    public static final String KEYCLOAK_LOCATION_HEADER_MISSING =
            "Location header missing from Keycloak response";

    // ============================================
    // Validation Error Messages
    // ============================================

    public static final String FIELD_REQUIRED = "%s is required";
    public static final String FIELD_MUST_BE_POSITIVE = "%s must be a positive number";
    public static final String DATE_RANGE_INVALID =
            "Start date must be before or equal to end date";
    public static final String INVALID_UUID_FORMAT = "Invalid UUID format: %s";

    // ============================================
    // Tenant / Multi-tenancy Error Messages
    // ============================================

    public static final String TENANT_CONTEXT_NOT_SET =
            "No tenant context set. Ensure TenantFilter is configured.";

    // ============================================
    // Compliance / Documents Error Messages
    // ============================================

    public static final String CONSENT_NOT_FOUND = "Consent not found with id: %s";
    public static final String CERTIFICATE_NOT_FOUND = "Certificate not found with id: %s";
    public static final String DOCUMENT_NOT_FOUND = "Document not found with id: %s";
    public static final String AUDIT_LOG_NOT_FOUND = "Audit log not found with id: %s";
}
