package com.vetclinic.compliance.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.common.tenant.TenantContext;
import com.vetclinic.compliance.domain.model.AuditAction;
import com.vetclinic.compliance.domain.model.AuditLog;
import com.vetclinic.compliance.domain.port.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepository {

    private final JpaAuditLogRepository jpaRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        if (auditLog.getClinicId() == null) {
            auditLog.setClinicId(TenantContext.getCurrentClinicId());
        }
        return jpaRepository.save(auditLog);
    }

    @Override
    public Optional<AuditLog> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<AuditLog> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId) {
        return jpaRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    @Override
    public List<AuditLog> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Override
    public List<AuditLog> findByAction(AuditAction action) {
        return jpaRepository.findByActionOrderByTimestampDesc(action);
    }

    @Override
    public List<AuditLog> findByTimestampBetween(Instant start, Instant end) {
        return jpaRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
    }

    @Override
    public List<AuditLog> findByEntityType(String entityType) {
        return jpaRepository.findByEntityTypeOrderByTimestampDesc(entityType);
    }
}
