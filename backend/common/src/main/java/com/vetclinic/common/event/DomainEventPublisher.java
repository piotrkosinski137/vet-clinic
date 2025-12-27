package com.vetclinic.common.event;

import java.util.Set;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.vetclinic.common.security.SecurityContextHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralized publisher for domain events. Automatically captures user and tenant context from the
 * current request.
 *
 * <p>Usage:
 *
 * <pre>
 * eventPublisher.publishCreated("Patient", patient.getId(), patient);
 * eventPublisher.publishUpdated("Patient", id, oldPatient, newPatient, changedFields);
 * eventPublisher.publishDeleted("Patient", id, patient);
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {

    private final ApplicationEventPublisher publisher;
    private final SecurityContextHelper securityContextHelper;

    /**
     * Publishes an event when a new entity is created.
     *
     * @param entityType The type of entity (e.g., "Patient", "Client")
     * @param entityId The ID of the created entity
     * @param entity The created entity object
     */
    public void publishCreated(String entityType, UUID entityId, Object entity) {
        EntityCreatedEvent event =
                new EntityCreatedEvent(
                        entityId,
                        entityType,
                        securityContextHelper.getCurrentClinicId(),
                        securityContextHelper.getCurrentUserId(),
                        securityContextHelper.getCurrentUserName(),
                        entity);
        publish(event);
    }

    /**
     * Publishes an event when an entity is updated.
     *
     * @param entityType The type of entity
     * @param entityId The ID of the updated entity
     * @param oldEntity The entity state before update
     * @param newEntity The entity state after update
     * @param changedFields Set of field names that changed
     */
    public void publishUpdated(
            String entityType,
            UUID entityId,
            Object oldEntity,
            Object newEntity,
            Set<String> changedFields) {
        EntityUpdatedEvent event =
                new EntityUpdatedEvent(
                        entityId,
                        entityType,
                        securityContextHelper.getCurrentClinicId(),
                        securityContextHelper.getCurrentUserId(),
                        securityContextHelper.getCurrentUserName(),
                        oldEntity,
                        newEntity,
                        changedFields);
        publish(event);
    }

    /**
     * Publishes an event when an entity is deleted.
     *
     * @param entityType The type of entity
     * @param entityId The ID of the deleted entity
     * @param entity The deleted entity object
     */
    public void publishDeleted(String entityType, UUID entityId, Object entity) {
        EntityDeletedEvent event =
                new EntityDeletedEvent(
                        entityId,
                        entityType,
                        securityContextHelper.getCurrentClinicId(),
                        securityContextHelper.getCurrentUserId(),
                        securityContextHelper.getCurrentUserName(),
                        entity);
        publish(event);
    }

    private void publish(DomainEvent event) {
        log.debug(
                "Publishing {} for {} with id {}",
                event.getClass().getSimpleName(),
                event.getAggregateType(),
                event.getAggregateId());
        publisher.publishEvent(event);
    }
}
