package com.vetclinic.veterinarian.domain;

import java.time.LocalDate;
import java.util.UUID;

/** Exception thrown when attempting to create a day off that conflicts with existing visits. */
public class DayOffConflictException extends RuntimeException {

    private final UUID veterinarianId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int visitCount;

    public DayOffConflictException(
            UUID veterinarianId, LocalDate startDate, LocalDate endDate, int visitCount) {
        super(
                String.format(
                        "Cannot create day off from %s to %s: %d existing visit(s) must be rescheduled first",
                        startDate, endDate, visitCount));
        this.veterinarianId = veterinarianId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.visitCount = visitCount;
    }

    public UUID getVeterinarianId() {
        return veterinarianId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getVisitCount() {
        return visitCount;
    }
}
