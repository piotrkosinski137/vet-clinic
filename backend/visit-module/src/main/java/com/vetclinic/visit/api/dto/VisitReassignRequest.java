package com.vetclinic.visit.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/** Request DTO for reassigning a visit to a different veterinarian and/or time. */
public record VisitReassignRequest(
        UUID veterinarianId,
        String veterinarianName,
        @NotNull(message = "New visit date/time is required") LocalDateTime visitDate) {}
