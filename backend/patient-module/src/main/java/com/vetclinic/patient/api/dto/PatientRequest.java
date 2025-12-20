package com.vetclinic.patient.api.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.vetclinic.patient.domain.model.Species;

public record PatientRequest(
        @NotBlank(message = "Name is required") String name,
        @NotNull(message = "Species is required") Species species,
        String breed,
        LocalDate dateOfBirth,
        @Positive(message = "Weight must be positive") Double weight,
        UUID ownerId,
        String notes) {}
