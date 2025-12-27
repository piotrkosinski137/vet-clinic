package com.vetclinic.visit.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.vetclinic.visit.domain.model.VisitStatus;
import com.vetclinic.visit.domain.model.VisitType;

/** Request DTO for creating/updating a visit. */
public record VisitRequest(
        @NotNull(message = "Patient ID is required") UUID patientId,
        UUID clientId,
        UUID veterinarianId,
        String veterinarianName,
        @NotNull(message = "Visit date is required") LocalDateTime visitDate,
        Integer durationMinutes,
        VisitStatus status,
        VisitType visitType,
        String reason,
        String interview,
        String examination,
        String diagnosis,
        String treatment,
        String recommendations,
        @Valid List<MedicationDto> medications,
        @Valid List<UsedMaterialDto> usedMaterials,
        String notes,
        Double weight,
        Double temperature,
        LocalDate nextVisitDate) {}
