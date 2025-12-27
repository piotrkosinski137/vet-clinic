package com.vetclinic.common.tenant;

import jakarta.persistence.PrePersist;

/**
 * JPA entity listener that automatically sets the clinic_id on new entities.
 *
 * <p>This ensures that all tenant-aware entities are properly associated with the current clinic
 * without requiring manual setting in service code.
 */
public class TenantEntityListener {

    /**
     * Sets the clinic_id from the current tenant context before persisting.
     *
     * @param entity the entity being persisted
     * @throws IllegalStateException if no tenant context is set
     */
    @PrePersist
    public void setClinicId(TenantAwareEntity entity) {
        if (entity.getClinicId() == null) {
            entity.setClinicId(TenantContext.requireCurrentClinicId());
        }
    }
}
