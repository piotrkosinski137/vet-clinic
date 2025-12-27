package com.vetclinic.compliance.domain.port;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.compliance.domain.model.AuditAction;
import com.vetclinic.compliance.domain.model.AuditLog;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    Optional<AuditLog> findById(UUID id);

    List<AuditLog> findAll();

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    List<AuditLog> findByUserId(UUID userId);

    List<AuditLog> findByAction(AuditAction action);

    List<AuditLog> findByTimestampBetween(Instant start, Instant end);

    List<AuditLog> findByEntityType(String entityType);
}
