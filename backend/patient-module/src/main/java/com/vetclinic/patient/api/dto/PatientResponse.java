package com.vetclinic.patient.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.vetclinic.patient.domain.model.Gender;
import com.vetclinic.patient.domain.model.PatientLabel;
import com.vetclinic.patient.domain.model.Species;

public record PatientResponse(
        UUID id,
        String name,
        Species species,
        String breed,
        LocalDate dateOfBirth,
        Double weight,
        UUID ownerId,
        String microchipNumber,
        String color,
        Gender gender,
        Boolean neutered,
        Set<PatientLabel> labels,
        String notes,
        Instant createdAt,
        Instant updatedAt) {}
