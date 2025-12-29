package com.vetclinic.visit.domain.model;

/**
 * Immutable snapshot of a medication for JSON storage in drafts. Mirrors the fields of Medication
 * but as a simple record for JSONB serialization.
 */
public record MedicationSnapshot(
        String name, String dosage, String frequency, String duration, String notes) {

    /** Convert from embeddable Medication to snapshot. */
    public static MedicationSnapshot from(Medication medication) {
        return new MedicationSnapshot(
                medication.getName(),
                medication.getDosage(),
                medication.getFrequency(),
                medication.getDuration(),
                medication.getNotes());
    }

    /** Convert to embeddable Medication. */
    public Medication toMedication() {
        return Medication.builder()
                .name(name)
                .dosage(dosage)
                .frequency(frequency)
                .duration(duration)
                .notes(notes)
                .build();
    }
}
