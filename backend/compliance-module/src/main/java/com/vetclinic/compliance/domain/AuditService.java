package com.vetclinic.compliance.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.compliance.domain.model.AuditAction;
import com.vetclinic.compliance.domain.model.AuditLog;
import com.vetclinic.compliance.domain.port.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public AuditLog createAuditLog(AuditLog auditLog) {
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(Instant.now());
        }
        return auditLogRepository.save(auditLog);
    }

    @Transactional
    public AuditLog logAction(
            String entityType,
            UUID entityId,
            AuditAction action,
            UUID userId,
            String userName,
            String description) {
        return logAction(
                entityType,
                entityId,
                action,
                userId,
                userName,
                description,
                Optional.empty(),
                Optional.empty());
    }

    @Transactional
    public AuditLog logAction(
            String entityType,
            UUID entityId,
            AuditAction action,
            UUID userId,
            String userName,
            String description,
            Optional<String> ipAddress,
            Optional<String> userAgent) {
        AuditLog auditLog =
                AuditLog.builder()
                        .entityType(entityType)
                        .entityId(entityId)
                        .action(action)
                        .userId(userId)
                        .userName(userName)
                        .timestamp(Instant.now())
                        .description(description)
                        .ipAddress(ipAddress.orElse(null))
                        .userAgent(userAgent.orElse(null))
                        .build();
        return auditLogRepository.save(auditLog);
    }

    @Transactional
    public AuditLog logChange(
            String entityType,
            UUID entityId,
            AuditAction action,
            UUID userId,
            String userName,
            String oldValue,
            String newValue,
            String changedFields,
            String description) {
        return logChange(
                entityType,
                entityId,
                action,
                userId,
                userName,
                oldValue,
                newValue,
                changedFields,
                description,
                Optional.empty(),
                Optional.empty());
    }

    @Transactional
    public AuditLog logChange(
            String entityType,
            UUID entityId,
            AuditAction action,
            UUID userId,
            String userName,
            String oldValue,
            String newValue,
            String changedFields,
            String description,
            Optional<String> ipAddress,
            Optional<String> userAgent) {
        AuditLog auditLog =
                AuditLog.builder()
                        .entityType(entityType)
                        .entityId(entityId)
                        .action(action)
                        .userId(userId)
                        .userName(userName)
                        .timestamp(Instant.now())
                        .oldValue(oldValue)
                        .newValue(newValue)
                        .changedFields(changedFields)
                        .description(description)
                        .ipAddress(ipAddress.orElse(null))
                        .userAgent(userAgent.orElse(null))
                        .build();
        return auditLogRepository.save(auditLog);
    }

    public AuditLog getAuditLog(UUID id) {
        return auditLogRepository.findById(id).orElseThrow(() -> new AuditLogNotFoundException(id));
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getAuditLogsForEntity(String entityType, UUID entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<AuditLog> getAuditLogsByUser(UUID userId) {
        return auditLogRepository.findByUserId(userId);
    }

    public List<AuditLog> getAuditLogsByAction(AuditAction action) {
        return auditLogRepository.findByAction(action);
    }

    public List<AuditLog> getAuditLogsByDateRange(Instant start, Instant end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }

    public List<AuditLog> getAuditLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityType(entityType);
    }
}
