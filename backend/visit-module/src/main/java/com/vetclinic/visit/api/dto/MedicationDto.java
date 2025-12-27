package com.vetclinic.visit.api.dto;

import jakarta.validation.constraints.NotBlank;

/** DTO for medication data. */
public record MedicationDto(
        @NotBlank(message = "Medication name is required") String name,
        String dosage,
        String frequency,
        String duration,
        String notes) {}
