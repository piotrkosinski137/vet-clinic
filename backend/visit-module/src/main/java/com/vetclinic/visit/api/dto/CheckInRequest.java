package com.vetclinic.visit.api.dto;

import jakarta.validation.constraints.Size;

import com.vetclinic.visit.domain.model.VisitPriority;

/** Request DTO for checking in a patient to the waiting room. */
public record CheckInRequest(
        @Size(max = 500, message = "Waiting room notes must not exceed 500 characters")
                String waitingRoomNotes,
        VisitPriority priority) {}
