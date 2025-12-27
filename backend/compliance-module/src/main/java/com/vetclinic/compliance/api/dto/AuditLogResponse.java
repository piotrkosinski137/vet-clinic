package com.vetclinic.compliance.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.vetclinic.compliance.domain.model.AuditAction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private UUID id;
    private String entityType;
    private UUID entityId;
    private AuditAction action;
    private UUID userId;
    private String userName;
    private Instant timestamp;
    private String oldValue;
    private String newValue;
    private String changedFields;
    private String ipAddress;
    private String userAgent;
    private String description;
}
