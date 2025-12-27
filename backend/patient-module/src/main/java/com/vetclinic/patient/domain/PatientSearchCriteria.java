package com.vetclinic.patient.domain;

import java.util.Set;
import java.util.UUID;

import com.vetclinic.patient.domain.model.PatientLabel;
import com.vetclinic.patient.domain.model.Species;

/**
 * Search criteria for finding patients. All fields are optional - only non-null values will be used
 * in the search.
 */
public record PatientSearchCriteria(
        /** Search by patient name (case-insensitive, partial match) */
        String name,
        /** Filter by species */
        Species species,
        /** Search by breed (case-insensitive, partial match) */
        String breed,
        /** Filter by owner ID */
        UUID ownerId,
        /** Search by microchip number (exact match) */
        String microchipNumber,
        /** Filter by labels - returns patients that have ANY of the specified labels */
        Set<PatientLabel> labels) {

    /** Create empty search criteria (returns all patients) */
    public static PatientSearchCriteria empty() {
        return new PatientSearchCriteria(null, null, null, null, null, null);
    }

    /** Check if any search criteria is specified */
    public boolean hasAnyCriteria() {
        return name != null
                || species != null
                || breed != null
                || ownerId != null
                || microchipNumber != null
                || (labels != null && !labels.isEmpty());
    }
}
