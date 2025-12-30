package com.vetclinic.billing.domain;

import java.util.UUID;

/** Exception thrown when inventory consumption fails during visit completion. */
public class InventoryConsumptionException extends RuntimeException {

    private final UUID visitId;

    public InventoryConsumptionException(UUID visitId, String message) {
        super(message);
        this.visitId = visitId;
    }

    public InventoryConsumptionException(UUID visitId, String message, Throwable cause) {
        super(message, cause);
        this.visitId = visitId;
    }

    public UUID getVisitId() {
        return visitId;
    }
}
