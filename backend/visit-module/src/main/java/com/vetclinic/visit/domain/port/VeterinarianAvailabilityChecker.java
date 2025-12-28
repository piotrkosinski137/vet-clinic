package com.vetclinic.visit.domain.port;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Port for checking veterinarian availability. This interface is implemented in the application
 * layer to avoid circular dependencies between modules.
 */
public interface VeterinarianAvailabilityChecker {

    /**
     * Checks if a veterinarian is working at the specified date and time.
     *
     * @param veterinarianId the veterinarian ID
     * @param dateTime the date and time to check
     * @return true if the veterinarian is working at that time, false otherwise
     */
    boolean isWorkingAt(UUID veterinarianId, LocalDateTime dateTime);

    /**
     * Checks if a veterinarian is on a day off on the specified date.
     *
     * @param veterinarianId the veterinarian ID
     * @param dateTime the date to check
     * @return true if the veterinarian is on day off, false otherwise
     */
    boolean isDayOff(UUID veterinarianId, LocalDateTime dateTime);
}
