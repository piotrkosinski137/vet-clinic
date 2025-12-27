package com.vetclinic.common.event;

import java.util.Set;
import java.util.UUID;

import lombok.Getter;

/**
 * Event published when an entity is updated. Contains both old and new values for change tracking
 * and audit purposes.
 */
@Getter
public class EntityUpdatedEvent extends DomainEvent {

    private final Object oldEntity;
    private final Object newEntity;
    private final Set<String> changedFields;

    public EntityUpdatedEvent(
            UUID aggregateId,
            String aggregateType,
            UUID clinicId,
            UUID userId,
            String userName,
            Object oldEntity,
            Object newEntity,
            Set<String> changedFields) {
        super(aggregateId, aggregateType, clinicId, userId, userName);
        this.oldEntity = oldEntity;
        this.newEntity = newEntity;
        this.changedFields = changedFields != null ? Set.copyOf(changedFields) : Set.of();
    }
}
