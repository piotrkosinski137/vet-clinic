package com.vetclinic.fixtures;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.vetclinic.patient.api.dto.PatientRequest;
import com.vetclinic.patient.domain.model.Gender;
import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.patient.domain.model.PatientLabel;
import com.vetclinic.patient.domain.model.Species;

/**
 * Test fixture builder for Patient entities. Provides sensible defaults and fluent API for creating
 * test data.
 */
public final class PatientFixture {

    private String name = "Buddy";
    private Species species = Species.DOG;
    private String breed = "Golden Retriever";
    private LocalDate dateOfBirth = LocalDate.of(2020, 5, 15);
    private Double weight = 30.0;
    private UUID ownerId = null;
    private String microchipNumber = null;
    private String color = null;
    private Gender gender = null;
    private Boolean neutered = null;
    private Set<PatientLabel> labels = new HashSet<>();
    private String notes = null;

    private PatientFixture() {}

    public static PatientFixture aPatient() {
        return new PatientFixture();
    }

    public static PatientFixture aDog() {
        return new PatientFixture().withSpecies(Species.DOG).withBreed("Labrador");
    }

    public static PatientFixture aCat() {
        return new PatientFixture()
                .withName("Whiskers")
                .withSpecies(Species.CAT)
                .withBreed("Persian")
                .withWeight(5.0);
    }

    public static PatientFixture aBird() {
        return new PatientFixture()
                .withName("Tweety")
                .withSpecies(Species.BIRD)
                .withBreed("Canary")
                .withWeight(0.05);
    }

    public PatientFixture withName(String name) {
        this.name = name;
        return this;
    }

    public PatientFixture withSpecies(Species species) {
        this.species = species;
        return this;
    }

    public PatientFixture withBreed(String breed) {
        this.breed = breed;
        return this;
    }

    public PatientFixture withDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public PatientFixture withAge(int years) {
        this.dateOfBirth = LocalDate.now().minusYears(years);
        return this;
    }

    public PatientFixture withWeight(Double weight) {
        this.weight = weight;
        return this;
    }

    public PatientFixture withOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public PatientFixture withMicrochipNumber(String microchipNumber) {
        this.microchipNumber = microchipNumber;
        return this;
    }

    public PatientFixture withColor(String color) {
        this.color = color;
        return this;
    }

    public PatientFixture withGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public PatientFixture withNeutered(Boolean neutered) {
        this.neutered = neutered;
        return this;
    }

    public PatientFixture withLabel(PatientLabel label) {
        this.labels.add(label);
        return this;
    }

    public PatientFixture withLabels(Set<PatientLabel> labels) {
        this.labels = new HashSet<>(labels);
        return this;
    }

    public PatientFixture withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    /** Builds a Patient entity with the configured values. */
    public Patient build() {
        return Patient.builder()
                .name(name)
                .species(species)
                .breed(breed)
                .dateOfBirth(dateOfBirth)
                .weight(weight)
                .ownerId(ownerId)
                .microchipNumber(microchipNumber)
                .color(color)
                .gender(gender)
                .neutered(neutered)
                .labels(labels)
                .notes(notes)
                .build();
    }

    /** Builds a PatientRequest DTO with the configured values. Useful for API testing. */
    public PatientRequest buildRequest() {
        return new PatientRequest(
                name,
                species,
                breed,
                dateOfBirth,
                weight,
                ownerId,
                microchipNumber,
                color,
                gender,
                neutered,
                labels.isEmpty() ? null : labels,
                notes);
    }

    /**
     * Returns JSON representation for API testing. Uses Jackson ObjectMapper for proper
     * serialization.
     */
    public String toJson() {
        return TestBuilders.toJson(buildRequest());
    }
}
