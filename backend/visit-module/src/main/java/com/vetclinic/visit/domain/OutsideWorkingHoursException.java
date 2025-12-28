package com.vetclinic.visit.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/** Exception thrown when attempting to schedule a visit outside of veterinarian's working hours. */
public class OutsideWorkingHoursException extends RuntimeException {

    private final UUID veterinarianId;
    private final LocalDateTime requestedTime;
    private final String reason;

    public OutsideWorkingHoursException(
            UUID veterinarianId, LocalDateTime requestedTime, String reason) {
        super(String.format("Cannot schedule visit at %s: %s", requestedTime.toString(), reason));
        this.veterinarianId = veterinarianId;
        this.requestedTime = requestedTime;
        this.reason = reason;
    }

    public static OutsideWorkingHoursException dayOff(
            UUID veterinarianId, LocalDateTime requestedTime) {
        return new OutsideWorkingHoursException(
                veterinarianId, requestedTime, "veterinarian is on day off");
    }

    public static OutsideWorkingHoursException outsideHours(
            UUID veterinarianId, LocalDateTime requestedTime) {
        return new OutsideWorkingHoursException(
                veterinarianId, requestedTime, "time is outside working hours");
    }

    public UUID getVeterinarianId() {
        return veterinarianId;
    }

    public LocalDateTime getRequestedTime() {
        return requestedTime;
    }

    public String getReason() {
        return reason;
    }
}
