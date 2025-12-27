package com.vetclinic.common.security;

/**
 * Security role constants used for role-based access control (RBAC). These roles are defined in
 * Keycloak and extracted from JWT tokens.
 */
public final class Roles {

    private Roles() {
        // Utility class - prevent instantiation
    }

    // ===========================================
    // Role Names (without ROLE_ prefix for Keycloak)
    // ===========================================

    /** Administrator with full system access. Can manage clinic settings, users, and all data. */
    public static final String ADMIN = "admin";

    /** Veterinarian role. Can manage own visits, patients, and medical records. */
    public static final String VET = "vet";

    /** Receptionist role. Can manage appointments, clients, and basic patient info. */
    public static final String RECEPTIONIST = "receptionist";

    /** Accountant role. Can manage invoices, payments, and financial reports. */
    public static final String ACCOUNTANT = "accountant";

    /** Basic user role. Read-only access to most resources. */
    public static final String USER = "user";

    // ===========================================
    // Spring Security Expression Constants
    // Note: Keycloak roles are uppercased by KeycloakRealmRoleConverter
    // ===========================================

    /** SpEL expression for admin-only access. */
    public static final String HAS_ADMIN = "hasRole('ADMIN')";

    /** SpEL expression for veterinarian or admin access. */
    public static final String HAS_VET_OR_ADMIN = "hasAnyRole('ADMIN', 'VET')";

    /** SpEL expression for receptionist, vet, or admin access. */
    public static final String HAS_RECEPTIONIST_OR_ABOVE =
            "hasAnyRole('ADMIN', 'VET', 'RECEPTIONIST')";

    /** SpEL expression for accountant or admin access. */
    public static final String HAS_ACCOUNTANT_OR_ADMIN = "hasAnyRole('ADMIN', 'ACCOUNTANT')";

    /** SpEL expression for any authenticated user. */
    public static final String HAS_ANY_ROLE =
            "hasAnyRole('ADMIN', 'VET', 'RECEPTIONIST', 'ACCOUNTANT', 'USER')";

    // ===========================================
    // Combined Expressions for Specific Operations
    // ===========================================

    /** Can manage clients (create, update, delete). Vets and receptionists can manage clients. */
    public static final String CAN_MANAGE_CLIENTS = HAS_RECEPTIONIST_OR_ABOVE;

    /** Can manage patients (create, update, delete). */
    public static final String CAN_MANAGE_PATIENTS = HAS_VET_OR_ADMIN;

    /** Can manage visits (create, update, delete). */
    public static final String CAN_MANAGE_VISITS = HAS_VET_OR_ADMIN;

    /** Can manage invoices (create, update, delete). */
    public static final String CAN_MANAGE_INVOICES = HAS_ACCOUNTANT_OR_ADMIN;

    /** Can manage veterinarians (create, update, delete). */
    public static final String CAN_MANAGE_VETS = HAS_ADMIN;

    /** Can view financial data (invoices, payments, reports). */
    public static final String CAN_VIEW_FINANCIALS = HAS_ACCOUNTANT_OR_ADMIN;

    /** Can manage inventory (stock, price list). */
    public static final String CAN_MANAGE_INVENTORY = HAS_ACCOUNTANT_OR_ADMIN;

    /** Can view audit logs. */
    public static final String CAN_VIEW_AUDIT = HAS_ADMIN;
}
