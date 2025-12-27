package com.vetclinic.common.event;

import java.util.UUID;

import lombok.Getter;

/**
 * Event published when an entity is deleted. Contains the deleted entity for audit logging
 * purposes.
 */
@Getter
public class EntityDeletedEvent extends DomainEvent {

    private final Object deletedEntity;

    public EntityDeletedEvent(
            UUID aggregateId,
            String aggregateType,
            UUID clinicId,
            UUID userId,
            String userName,
            Object deletedEntity) {
        super(aggregateId, aggregateType, clinicId, userId, userName);
        this.deletedEntity = deletedEntity;
    }
}
