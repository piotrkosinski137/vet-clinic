package com.vetclinic.veterinarian.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.vetclinic.common.tenant.TenantAwareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A veterinarian (staff member) in the veterinary clinic. */
@Entity
@Table(name = "veterinarians")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Veterinarian extends TenantAwareEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    /** Specialization/specialty of the veterinarian (e.g., Surgery, Dermatology) */
    private String specialization;

    /** License number for regulatory compliance */
    @Column(name = "license_number")
    private String licenseNumber;

    /** Color code for calendar display (hex format, e.g., #4CAF50) */
    @Column(name = "color_code", length = 7)
    private String colorCode;

    /** Whether this veterinarian is currently active */
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    /** Additional notes about the veterinarian */
    private String notes;

    /** Get full name for display purposes */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
