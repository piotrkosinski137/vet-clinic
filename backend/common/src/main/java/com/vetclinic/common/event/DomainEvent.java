package com.vetclinic.common.event;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;

/**
 * Base class for all domain events in the application. Domain events are used to notify other parts
 * of the system when something significant happens.
 *
 * <p>Events are immutable and carry all the context needed for event handlers.
 */
@Getter
public abstract class DomainEvent {

    private final UUID eventId;
    private final Instant occurredAt;
    private final UUID aggregateId;
    private final String aggregateType;
    private final UUID clinicId;
    private final UUID userId;
    private final String userName;

    protected DomainEvent(
            UUID aggregateId, String aggregateType, UUID clinicId, UUID userId, String userName) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.clinicId = clinicId;
        this.userId = userId;
        this.userName = userName;
    }
}
