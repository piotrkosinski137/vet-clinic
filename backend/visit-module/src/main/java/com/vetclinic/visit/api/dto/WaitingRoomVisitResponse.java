package com.vetclinic.visit.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.vetclinic.visit.domain.model.VisitPriority;
import com.vetclinic.visit.domain.model.VisitType;

/**
 * Enriched response DTO for visits in the waiting room. Contains all information needed for the
 * waiting room display.
 */
public record WaitingRoomVisitResponse(
        // Visit identification
        UUID id,
        LocalDateTime visitDate,
        VisitType visitType,
        String reason,

        // Waiting room specific
        LocalDateTime checkedInAt,
        long waitingTimeMinutes,
        String waitingRoomNotes,
        VisitPriority priority,

        // Patient info
        UUID patientId,
        String patientName,
        String species,
        String breed,
        List<String> patientLabels,

        // Client info
        UUID clientId,
        String clientName,
        String clientPhone,

        // Veterinarian info
        UUID veterinarianId,
        String veterinarianName,

        // Financial info
        BigDecimal estimatedCost,
        BigDecimal clientDebt,
        BigDecimal totalToPay) {}
