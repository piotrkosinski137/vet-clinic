package com.vetclinic.compliance.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.vetclinic.compliance.api.dto.AuditLogRequest;
import com.vetclinic.compliance.api.dto.AuditLogResponse;
import com.vetclinic.compliance.domain.model.AuditLog;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuditLogMapper {

    AuditLog toEntity(AuditLogRequest request);

    AuditLogResponse toResponse(AuditLog auditLog);

    List<AuditLogResponse> toResponseList(List<AuditLog> auditLogs);
}
