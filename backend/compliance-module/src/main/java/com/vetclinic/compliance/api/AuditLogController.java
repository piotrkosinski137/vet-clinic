package com.vetclinic.compliance.api;

import static com.vetclinic.common.security.Roles.CAN_VIEW_AUDIT;
import static com.vetclinic.common.security.Roles.HAS_ADMIN;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.compliance.api.dto.AuditLogRequest;
import com.vetclinic.compliance.api.dto.AuditLogResponse;
import com.vetclinic.compliance.domain.AuditService;
import com.vetclinic.compliance.domain.model.AuditAction;
import com.vetclinic.compliance.domain.model.AuditLog;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;
    private final AuditLogMapper mapper;

    @PostMapping
    @PreAuthorize(HAS_ADMIN)
    public ResponseEntity<AuditLogResponse> createAuditLog(
            @Valid @RequestBody AuditLogRequest request) {
        AuditLog auditLog = mapper.toEntity(request);
        AuditLog created = auditService.createAuditLog(auditLog);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    @PreAuthorize(CAN_VIEW_AUDIT)
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {
        List<AuditLog> auditLogs = auditService.getAllAuditLogs();
        return ResponseEntity.ok(mapper.toResponseList(auditLogs));
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_VIEW_AUDIT)
    public ResponseEntity<AuditLogResponse> getAuditLog(@PathVariable UUID id) {
        AuditLog auditLog = auditService.getAuditLog(id);
        return ResponseEntity.ok(mapper.toResponse(auditLog));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize(CAN_VIEW_AUDIT)
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsForEntity(
            @PathVariable String entityType, @PathVariable UUID entityId) {
        List<AuditLog> auditLogs = auditService.getAuditLogsForEntity(entityType, entityId);
        return ResponseEntity.ok(mapper.toResponseList(auditLogs));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize(CAN_VIEW_AUDIT)
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByUser(@PathVariable UUID userId) {
        List<AuditLog> auditLogs = auditService.getAuditLogsByUser(userId);
        return ResponseEntity.ok(mapper.toResponseList(auditLogs));
    }

    @GetMapping("/action/{action}")
    @PreAuthorize(CAN_VIEW_AUDIT)
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByAction(
            @PathVariable AuditAction action) {
        List<AuditLog> auditLogs = auditService.getAuditLogsByAction(action);
        return ResponseEntity.ok(mapper.toResponseList(auditLogs));
    }

    @GetMapping("/date-range")
    @PreAuthorize(CAN_VIEW_AUDIT)
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByDateRange(
            @RequestParam Instant start, @RequestParam Instant end) {
        List<AuditLog> auditLogs = auditService.getAuditLogsByDateRange(start, end);
        return ResponseEntity.ok(mapper.toResponseList(auditLogs));
    }

    @GetMapping("/entity-type/{entityType}")
    @PreAuthorize(CAN_VIEW_AUDIT)
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByEntityType(
            @PathVariable String entityType) {
        List<AuditLog> auditLogs = auditService.getAuditLogsByEntityType(entityType);
        return ResponseEntity.ok(mapper.toResponseList(auditLogs));
    }
}
