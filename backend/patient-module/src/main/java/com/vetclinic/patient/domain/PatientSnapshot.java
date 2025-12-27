package com.vetclinic.patient.domain;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.vetclinic.patient.domain.model.Gender;
import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.model.PatientLabel;
import com.vetclinic.patient.domain.model.Species;

/**
 * Immutable snapshot of a Patient for audit logging. Avoids JPA entity serialization issues and
 * circular references.
 */
public record PatientSnapshot(
        UUID id,
        String name,
        Species species,
        String breed,
        LocalDate dateOfBirth,
        Double weight,
        String microchipNumber,
        String color,
        Gender gender,
        Boolean neutered,
        Set<PatientLabel> labels,
        String notes,
        UUID ownerId) {

    public static PatientSnapshot from(Patient patient) {
        return new PatientSnapshot(
                patient.getId(),
                patient.getName(),
                patient.getSpecies(),
                patient.getBreed(),
                patient.getDateOfBirth(),
                patient.getWeight(),
                patient.getMicrochipNumber(),
                patient.getColor(),
                patient.getGender(),
                patient.getNeutered(),
                patient.getLabels() != null
                        ? patient.getLabels().stream().collect(Collectors.toSet())
                        : Set.of(),
                patient.getNotes(),
                patient.getOwnerId());
    }
}
