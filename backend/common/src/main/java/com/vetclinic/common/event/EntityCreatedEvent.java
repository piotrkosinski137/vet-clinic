package com.vetclinic.common.event;

import java.util.UUID;

import lombok.Getter;

/**
 * Event published when a new entity is created. Contains the newly created entity for audit logging
 * purposes.
 */
@Getter
public class EntityCreatedEvent extends DomainEvent {

    private final Object entity;

    public EntityCreatedEvent(
            UUID aggregateId,
            String aggregateType,
            UUID clinicId,
            UUID userId,
            String userName,
            Object entity) {
        super(aggregateId, aggregateType, clinicId, userId, userName);
        this.entity = entity;
    }
}
