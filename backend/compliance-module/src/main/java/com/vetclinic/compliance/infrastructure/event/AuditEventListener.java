package com.vetclinic.compliance.infrastructure.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.vetclinic.common.event.EntityCreatedEvent;
import com.vetclinic.common.event.EntityDeletedEvent;
import com.vetclinic.common.event.EntityUpdatedEvent;
import com.vetclinic.common.security.SecurityContextHelper;
import com.vetclinic.compliance.domain.AuditService;
import com.vetclinic.compliance.domain.model.AuditAction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listens for domain events and creates audit log entries. Uses REQUIRES_NEW propagation to ensure
 * audit logs are persisted even if the main transaction fails.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final SecurityContextHelper securityContextHelper;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEntityCreated(EntityCreatedEvent event) {
        log.debug(
                "Handling EntityCreatedEvent for {} with id {}",
                event.getAggregateType(),
                event.getAggregateId());

        auditService.logAction(
                event.getAggregateType(),
                event.getAggregateId(),
                AuditAction.CREATE,
                event.getUserId(),
                event.getUserName(),
                "Created " + event.getAggregateType(),
                securityContextHelper.getIpAddress(),
                securityContextHelper.getUserAgent());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEntityUpdated(EntityUpdatedEvent event) {
        log.debug(
                "Handling EntityUpdatedEvent for {} with id {}",
                event.getAggregateType(),
                event.getAggregateId());

        String oldJson = toJson(event.getOldEntity());
        String newJson = toJson(event.getNewEntity());
        String changedFields = String.join(",", event.getChangedFields());

        auditService.logChange(
                event.getAggregateType(),
                event.getAggregateId(),
                AuditAction.UPDATE,
                event.getUserId(),
                event.getUserName(),
                oldJson,
                newJson,
                changedFields,
                "Updated " + event.getAggregateType() + " - fields: " + changedFields,
                securityContextHelper.getIpAddress(),
                securityContextHelper.getUserAgent());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEntityDeleted(EntityDeletedEvent event) {
        log.debug(
                "Handling EntityDeletedEvent for {} with id {}",
                event.getAggregateType(),
                event.getAggregateId());

        String entityJson = toJson(event.getDeletedEntity());

        auditService.logChange(
                event.getAggregateType(),
                event.getAggregateId(),
                AuditAction.DELETE,
                event.getUserId(),
                event.getUserName(),
                entityJson,
                null,
                null,
                "Deleted " + event.getAggregateType(),
                securityContextHelper.getIpAddress(),
                securityContextHelper.getUserAgent());
    }

    private String toJson(Object entity) {
        if (entity == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize entity to JSON: {}", e.getMessage());
            return entity.toString();
        }
    }
}
