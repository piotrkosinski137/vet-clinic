package com.vetclinic.common.tenant;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import com.vetclinic.common.domain.BaseEntity;
import com.vetclinic.common.domain.VeterinaryClinic;

import lombok.Getter;
import lombok.Setter;

/**
 * Base entity for all tenant-aware (clinic-scoped) entities.
 *
 * <p>Entities extending this class will:
 *
 * <ul>
 *   <li>Have a clinic_id column linking to the veterinary clinic
 *   <li>Be automatically filtered by clinic in queries (via Hibernate filter)
 *   <li>Have clinic_id auto-set on persist (via {@link TenantEntityListener})
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>
 * &#64;Entity
 * public class Patient extends TenantAwareEntity {
 *     // Patient-specific fields
 * }
 * </pre>
 */
@MappedSuperclass
@EntityListeners(TenantEntityListener.class)
@FilterDef(
        name = TenantAwareEntity.TENANT_FILTER_NAME,
        parameters = @ParamDef(name = "clinicId", type = UUID.class))
@Filter(name = TenantAwareEntity.TENANT_FILTER_NAME, condition = "clinic_id = :clinicId")
@Getter
@Setter
public abstract class TenantAwareEntity extends BaseEntity {

    public static final String TENANT_FILTER_NAME = "tenantFilter";

    @Column(name = "clinic_id", nullable = false, updatable = false)
    private UUID clinicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", insertable = false, updatable = false)
    private VeterinaryClinic clinic;
}
