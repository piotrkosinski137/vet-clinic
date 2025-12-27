package com.vetclinic.compliance.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetclinic.compliance.domain.model.AuditAction;
import com.vetclinic.compliance.domain.model.AuditLog;

public interface JpaAuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(
            String entityType, UUID entityId);

    List<AuditLog> findByUserIdOrderByTimestampDesc(UUID userId);

    List<AuditLog> findByActionOrderByTimestampDesc(AuditAction action);

    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(Instant start, Instant end);

    List<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType);
}
