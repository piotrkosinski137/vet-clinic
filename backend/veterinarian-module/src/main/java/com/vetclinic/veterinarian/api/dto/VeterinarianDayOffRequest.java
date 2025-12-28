package com.vetclinic.veterinarian.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

import com.vetclinic.veterinarian.domain.model.DayOffType;

import lombok.Builder;

@Builder
public record VeterinarianDayOffRequest(
        @NotNull(message = "Start date is required") LocalDate startDate,
        @NotNull(message = "End date is required") LocalDate endDate,
        @NotNull(message = "Type is required") DayOffType type,
        String description) {}
