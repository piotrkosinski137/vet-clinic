package com.vetclinic.veterinarian.domain.port;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Port for checking visits associated with a veterinarian. This interface is implemented in the
 * application layer to avoid circular dependencies between modules.
 */
public interface VeterinarianVisitChecker {

    /**
     * Counts the number of active (non-cancelled) visits for a veterinarian in the given date
     * range.
     *
     * @param veterinarianId the veterinarian ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return the count of active visits in the range
     */
    int countVisitsInDateRange(UUID veterinarianId, LocalDate startDate, LocalDate endDate);
}
