package com.vetclinic.patient.api.dto;

import com.vetclinic.patient.domain.model.Species;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PatientResponse {

    UUID id;
    String name;
    Species species;
    String breed;
    LocalDate dateOfBirth;
    Double weight;
    UUID ownerId;
    String notes;
    Instant createdAt;
    Instant updatedAt;
}
