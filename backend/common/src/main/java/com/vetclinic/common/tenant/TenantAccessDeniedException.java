package com.vetclinic.common.tenant;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to access a resource belonging to a different clinic
 * (tenant). This is a security exception that results in a 403 Forbidden response.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TenantAccessDeniedException extends RuntimeException {

    private final UUID requestedClinicId;
    private final UUID userClinicId;
    private final String resourceType;
    private final UUID resourceId;

    public TenantAccessDeniedException(String message) {
        super(message);
        this.requestedClinicId = null;
        this.userClinicId = null;
        this.resourceType = null;
        this.resourceId = null;
    }

    public TenantAccessDeniedException(
            UUID requestedClinicId, UUID userClinicId, String resourceType, UUID resourceId) {
        super(
                String.format(
                        "Access denied: %s with ID %s belongs to clinic %s, but user is in clinic"
                                + " %s",
                        resourceType, resourceId, requestedClinicId, userClinicId));
        this.requestedClinicId = requestedClinicId;
        this.userClinicId = userClinicId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public UUID getRequestedClinicId() {
        return requestedClinicId;
    }

    public UUID getUserClinicId() {
        return userClinicId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }
}
