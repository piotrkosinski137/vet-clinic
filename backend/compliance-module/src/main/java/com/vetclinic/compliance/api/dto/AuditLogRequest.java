package com.vetclinic.compliance.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
public class AuditLogRequest {

    @NotBlank(message = "Entity type is required")
    private String entityType;

    @NotNull(message = "Entity ID is required")
    private UUID entityId;

    @NotNull(message = "Action is required")
    private AuditAction action;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "User name is required")
    private String userName;

    private String oldValue;

    private String newValue;

    private String changedFields;

    private String ipAddress;

    private String userAgent;

    private String description;
}
