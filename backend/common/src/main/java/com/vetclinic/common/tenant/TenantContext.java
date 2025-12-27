package com.vetclinic.common.tenant;

import java.util.UUID;

/**
 * Holds the current tenant (clinic) context for the request thread.
 *
 * <p>This is set by {@link TenantFilter} at the start of each request and used by:
 *
 * <ul>
 *   <li>Hibernate filters to automatically filter queries by clinic
 *   <li>Entity listeners to auto-set clinic_id on new entities
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>
 * UUID clinicId = TenantContext.getCurrentClinicId();
 * </pre>
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> currentClinicId = new ThreadLocal<>();

    private TenantContext() {}

    /** Returns the current clinic ID for this request thread, or null if not set. */
    public static UUID getCurrentClinicId() {
        return currentClinicId.get();
    }

    /** Sets the current clinic ID for this request thread. */
    public static void setCurrentClinicId(UUID clinicId) {
        currentClinicId.set(clinicId);
    }

    /** Clears the current clinic ID. Should be called at the end of each request. */
    public static void clear() {
        currentClinicId.remove();
    }

    /** Returns true if a clinic context is currently set. */
    public static boolean isSet() {
        return currentClinicId.get() != null;
    }

    /**
     * Returns the current clinic ID or throws if not set.
     *
     * @throws IllegalStateException if no clinic context is set
     */
    public static UUID requireCurrentClinicId() {
        UUID clinicId = currentClinicId.get();
        if (clinicId == null) {
            throw new IllegalStateException(
                    "No tenant context set. Ensure TenantFilter is configured.");
        }
        return clinicId;
    }

    /**
     * Verifies that the given clinic ID matches the current tenant context.
     *
     * @param entityClinicId the clinic ID of the entity being accessed
     * @param resourceType the type of resource (e.g., "Client", "Patient")
     * @param resourceId the ID of the resource
     * @throws TenantAccessDeniedException if the clinic IDs don't match
     */
    public static void verifyAccess(UUID entityClinicId, String resourceType, UUID resourceId) {
        UUID currentClinic = getCurrentClinicId();
        if (currentClinic == null) {
            throw new IllegalStateException(
                    "No tenant context set. Ensure TenantFilter is configured.");
        }
        if (!currentClinic.equals(entityClinicId)) {
            throw new TenantAccessDeniedException(
                    entityClinicId, currentClinic, resourceType, resourceId);
        }
    }

    /**
     * Checks if the given clinic ID matches the current tenant context.
     *
     * @param entityClinicId the clinic ID to check
     * @return true if the clinic IDs match, false otherwise
     */
    public static boolean hasAccess(UUID entityClinicId) {
        UUID currentClinic = getCurrentClinicId();
        return currentClinic != null && currentClinic.equals(entityClinicId);
    }
}
