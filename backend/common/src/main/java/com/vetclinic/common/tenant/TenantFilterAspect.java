package com.vetclinic.common.tenant;

import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Aspect that enables the Hibernate tenant filter for all repository operations.
 *
 * <p>This ensures all queries on tenant-aware entities are automatically filtered by the current
 * clinic, preventing data leakage between tenants.
 */
@Aspect
@Component
public class TenantFilterAspect {

    private final EntityManager entityManager;

    public TenantFilterAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Enables the tenant filter before any repository method execution.
     *
     * <p>Matches all methods in classes ending with "Repository" in any vetclinic package.
     */
    @Before(
            "execution(* com.vetclinic..*.repository.*.*(..)) || "
                    + "execution(* com.vetclinic..*.persistence.*.*(..))")
    public void enableTenantFilter() {
        UUID clinicId = TenantContext.getCurrentClinicId();
        if (clinicId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter(TenantAwareEntity.TENANT_FILTER_NAME)
                    .setParameter("clinicId", clinicId);
        }
    }
}
