package com.vetclinic.veterinarian.api.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record VeterinarianScheduleResponse(
        UUID id,
        UUID veterinarianId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean workingDay) {}
