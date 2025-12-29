package com.vetclinic.visit.domain;

import java.time.LocalDate;
import java.util.UUID;

import com.vetclinic.visit.domain.model.VisitStatus;

/**
 * Search criteria for finding visits. All fields are optional - only non-null values will be used
 * in the search.
 */
public record VisitSearchCriteria(
        /** Filter by patient ID */
        UUID patientId,
        /** Filter by client ID */
        UUID clientId,
        /** Filter by visit status */
        VisitStatus status,
        /** Filter by visit date - from (inclusive) */
        LocalDate dateFrom,
        /** Filter by visit date - to (inclusive) */
        LocalDate dateTo) {

    /** Create empty search criteria (returns all visits) */
    public static VisitSearchCriteria empty() {
        return new VisitSearchCriteria(null, null, null, null, null);
    }

    /** Check if any search criteria is specified */
    public boolean hasAnyCriteria() {
        return patientId != null
                || clientId != null
                || status != null
                || dateFrom != null
                || dateTo != null;
    }

    /** Create criteria for a specific patient */
    public static VisitSearchCriteria forPatient(UUID patientId) {
        return new VisitSearchCriteria(patientId, null, null, null, null);
    }

    /** Create criteria for a specific client */
    public static VisitSearchCriteria forClient(UUID clientId) {
        return new VisitSearchCriteria(null, clientId, null, null, null);
    }

    /** Create criteria for a date range */
    public static VisitSearchCriteria forDateRange(LocalDate from, LocalDate to) {
        return new VisitSearchCriteria(null, null, null, from, to);
    }

    /** Create criteria for a specific status */
    public static VisitSearchCriteria forStatus(VisitStatus status) {
        return new VisitSearchCriteria(null, null, status, null, null);
    }
}
