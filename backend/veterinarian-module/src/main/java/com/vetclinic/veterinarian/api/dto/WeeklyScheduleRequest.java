package com.vetclinic.veterinarian.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Builder;

@Builder
public record WeeklyScheduleRequest(
        @NotNull(message = "Schedules are required")
                @Size(
                        min = 7,
                        max = 7,
                        message = "Must provide exactly 7 schedule entries (one per day)")
                @Valid
                List<VeterinarianScheduleRequest> schedules) {}
