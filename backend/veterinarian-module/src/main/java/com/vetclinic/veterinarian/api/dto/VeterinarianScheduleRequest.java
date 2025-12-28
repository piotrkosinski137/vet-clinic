package com.vetclinic.veterinarian.api.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;

@Builder
public record VeterinarianScheduleRequest(
        @NotNull(message = "Day of week is required") DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean workingDay) {}
