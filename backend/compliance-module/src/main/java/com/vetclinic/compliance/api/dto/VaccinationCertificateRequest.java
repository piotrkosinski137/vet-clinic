package com.vetclinic.compliance.api.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
public class VaccinationCertificateRequest {

    @NotNull(message = "Certificate type is required")
    private CertificateType certificateType;

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotBlank(message = "Patient name is required")
    private String patientName;

    private String patientSpecies;

    private String patientBreed;

    private String microchipNumber;

    @NotNull(message = "Client ID is required")
    private UUID clientId;

    @NotBlank(message = "Client name is required")
    private String clientName;

    private UUID visitId;

    @NotBlank(message = "Vaccine name is required")
    private String vaccineName;

    private String vaccineManufacturer;

    private String batchNumber;

    private LocalDate administrationDate;

    private LocalDate expirationDate;

    private LocalDate nextDueDate;

    private UUID veterinarianId;

    @NotBlank(message = "Veterinarian name is required")
    private String veterinarianName;

    private String veterinarianLicenseNumber;

    private String notes;
}
