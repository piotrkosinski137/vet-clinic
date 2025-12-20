package com.vetclinic.fixtures;

import java.time.LocalDate;
import java.util.UUID;

import com.vetclinic.patient.domain.model.Patient;
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

    public PatientFixture withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public Patient build() {
        return Patient.builder()
                .name(name)
                .species(species)
                .breed(breed)
                .dateOfBirth(dateOfBirth)
                .weight(weight)
                .ownerId(ownerId)
                .notes(notes)
                .build();
    }

    /** Returns JSON representation for API testing. */
    public String toJson() {
        StringBuilder json = new StringBuilder("{");
        json.append("\"name\":\"").append(name).append("\"");
        json.append(",\"species\":\"").append(species.name()).append("\"");

        if (breed != null) {
            json.append(",\"breed\":\"").append(breed).append("\"");
        }
        if (dateOfBirth != null) {
            json.append(",\"dateOfBirth\":\"").append(dateOfBirth).append("\"");
        }
        if (weight != null) {
            json.append(",\"weight\":").append(weight);
        }
        if (ownerId != null) {
            json.append(",\"ownerId\":\"").append(ownerId).append("\"");
        }
        if (notes != null) {
            json.append(",\"notes\":\"").append(notes).append("\"");
        }

        json.append("}");
        return json.toString();
    }
}
