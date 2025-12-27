package com.vetclinic.visit.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Exception thrown when an appointment conflicts with an existing one for the same veterinarian.
 */
public class AppointmentConflictException extends RuntimeException {

    private final UUID veterinarianId;
    private final LocalDateTime requestedTime;

    public AppointmentConflictException(UUID veterinarianId, LocalDateTime requestedTime) {
        super(
                String.format(
                        "This veterinarian already has an overlapping appointment at %s. Please choose a different time slot.",
                        requestedTime.toLocalTime()));
        this.veterinarianId = veterinarianId;
        this.requestedTime = requestedTime;
    }

    public UUID getVeterinarianId() {
        return veterinarianId;
    }

    public LocalDateTime getRequestedTime() {
        return requestedTime;
    }
}
