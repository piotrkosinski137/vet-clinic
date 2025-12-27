package com.vetclinic.compliance.domain.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import com.vetclinic.common.tenant.TenantAwareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vaccination_certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccinationCertificate extends TenantAwareEntity {

    @Column(nullable = false, unique = true)
    private String certificateNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateType certificateType;

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    private String patientName;

    private String patientSpecies;

    private String patientBreed;

    private String microchipNumber;

    @Column(nullable = false)
    private UUID clientId;

    @Column(nullable = false)
    private String clientName;

    private UUID visitId;

    @Column(nullable = false)
    private String vaccineName;

    private String vaccineManufacturer;

    private String batchNumber;

    @Column(nullable = false)
    private LocalDate administrationDate;

    private LocalDate expirationDate;

    private LocalDate nextDueDate;

    private UUID veterinarianId;

    @Column(nullable = false)
    private String veterinarianName;

    private String veterinarianLicenseNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isValid = true;

    private String invalidationReason;

    public boolean isExpired() {
        if (expirationDate == null) {
            return false;
        }
        return expirationDate.isBefore(LocalDate.now());
    }
}
