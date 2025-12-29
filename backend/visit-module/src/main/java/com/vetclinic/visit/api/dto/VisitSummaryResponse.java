package com.vetclinic.visit.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.vetclinic.visit.domain.model.VisitStatus;

/** Response DTO for printable visit summary. */
public record VisitSummaryResponse(
        UUID visitId,
        UUID patientId,
        LocalDateTime visitDate,
        VisitStatus status,
        String reason,
        String examination,
        String diagnosis,
        String treatment,
        String recommendations,
        List<MedicationDto> medications,
        Double weight,
        Double temperature,
        LocalDateTime nextVisitDate,
        String generatedAt) {}
