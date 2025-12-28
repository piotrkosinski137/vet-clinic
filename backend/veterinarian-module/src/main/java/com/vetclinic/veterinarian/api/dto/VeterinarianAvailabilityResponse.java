package com.vetclinic.veterinarian.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.vetclinic.veterinarian.domain.model.DayOffType;

import lombok.Builder;

@Builder
public record VeterinarianAvailabilityResponse(
        UUID veterinarianId,
        LocalDate date,
        boolean workingDay,
        LocalTime startTime,
        LocalTime endTime,
        boolean isDayOff,
        DayOffType dayOffType,
        String dayOffDescription) {}
