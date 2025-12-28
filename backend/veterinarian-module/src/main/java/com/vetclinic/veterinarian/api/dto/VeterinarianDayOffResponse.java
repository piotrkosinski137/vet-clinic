package com.vetclinic.veterinarian.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.vetclinic.veterinarian.domain.model.DayOffType;

import lombok.Builder;

@Builder
public record VeterinarianDayOffResponse(
        UUID id,
        UUID veterinarianId,
        LocalDate startDate,
        LocalDate endDate,
        DayOffType type,
        String description,
        boolean approved,
        Instant createdAt) {}
