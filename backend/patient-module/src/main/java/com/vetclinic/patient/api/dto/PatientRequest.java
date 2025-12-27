package com.vetclinic.patient.api.dto;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import com.vetclinic.patient.domain.model.Gender;
import com.vetclinic.patient.domain.model.PatientLabel;
import com.vetclinic.patient.domain.model.Species;

public record PatientRequest(
        @NotBlank(message = "Name is required") String name,
        @NotNull(message = "Species is required") Species species,
        String breed,
        LocalDate dateOfBirth,
        @Positive(message = "Weight must be positive") Double weight,
        UUID ownerId,
        @Pattern(regexp = "^[0-9]{9,15}$", message = "Microchip must be 9-15 digits")
                String microchipNumber,
        String color,
        Gender gender,
        Boolean neutered,
        Set<PatientLabel> labels,
        String notes) {}
