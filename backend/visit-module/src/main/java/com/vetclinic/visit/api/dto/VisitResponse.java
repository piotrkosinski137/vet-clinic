package com.vetclinic.visit.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.vetclinic.visit.domain.model.VisitPriority;
import com.vetclinic.visit.domain.model.VisitStatus;
import com.vetclinic.visit.domain.model.VisitType;

/** Response DTO for visit data. */
public record VisitResponse(
        UUID id,
        UUID patientId,
        String patientName,
        UUID clientId,
        String clientName,
        UUID veterinarianId,
        String veterinarianName,
        LocalDateTime visitDate,
        Integer durationMinutes,
        VisitStatus status,
        VisitType visitType,
        String reason,
        String interview,
        String examination,
        String diagnosis,
        String treatment,
        String recommendations,
        List<MedicationDto> medications,
        List<UsedMaterialDto> usedMaterials,
        BigDecimal totalMaterialsCost,
        BigDecimal totalMaterialsSell,
        BigDecimal totalMaterialsProfit,
        String notes,
        Double weight,
        Double temperature,
        LocalDateTime nextVisitDate,
        UUID previousVisitId,
        // Waiting room fields
        LocalDateTime checkedInAt,
        String waitingRoomNotes,
        VisitPriority priority,
        Instant createdAt,
        Instant updatedAt) {}
