package com.vetclinic.visit.domain;

import com.vetclinic.visit.domain.model.VisitStatus;

/**
 * Exception thrown when a visit state transition is invalid. For example, trying to check in a
 * visit that is already in progress.
 */
public class InvalidVisitStateException extends RuntimeException {

    private final VisitStatus currentStatus;
    private final VisitStatus targetStatus;

    public InvalidVisitStateException(String message) {
        super(message);
        this.currentStatus = null;
        this.targetStatus = null;
    }

    public InvalidVisitStateException(VisitStatus currentStatus, VisitStatus targetStatus) {
        super(
                String.format(
                        "Cannot transition from %s to %s",
                        currentStatus.name(), targetStatus.name()));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public InvalidVisitStateException(
            VisitStatus currentStatus, VisitStatus targetStatus, String reason) {
        super(
                String.format(
                        "Cannot transition from %s to %s: %s",
                        currentStatus.name(), targetStatus.name(), reason));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public VisitStatus getCurrentStatus() {
        return currentStatus;
    }

    public VisitStatus getTargetStatus() {
        return targetStatus;
    }
}
