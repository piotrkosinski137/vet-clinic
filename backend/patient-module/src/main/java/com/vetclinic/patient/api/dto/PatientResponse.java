package com.vetclinic.patient.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.vetclinic.patient.domain.model.Species;

public record PatientResponse(
        UUID id,
        String name,
        Species species,
        String breed,
        LocalDate dateOfBirth,
        Double weight,
        UUID ownerId,
        String notes,
        Instant createdAt,
        Instant updatedAt) {}
