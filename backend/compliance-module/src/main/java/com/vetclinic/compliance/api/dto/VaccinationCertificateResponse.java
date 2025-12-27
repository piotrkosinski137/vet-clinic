package com.vetclinic.compliance.api.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.vetclinic.compliance.domain.model.CertificateType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccinationCertificateResponse {

    private UUID id;
    private String certificateNumber;
    private CertificateType certificateType;
    private UUID patientId;
    private String patientName;
    private String patientSpecies;
    private String patientBreed;
    private String microchipNumber;
    private UUID clientId;
    private String clientName;
    private UUID visitId;
    private String vaccineName;
    private String vaccineManufacturer;
    private String batchNumber;
    private LocalDate administrationDate;
    private LocalDate expirationDate;
    private LocalDate nextDueDate;
    private UUID veterinarianId;
    private String veterinarianName;
    private String veterinarianLicenseNumber;
    private String notes;
    private Boolean isValid;
    private String invalidationReason;
    private boolean expired;
}
