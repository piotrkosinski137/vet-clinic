package com.vetclinic.compliance.domain;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.vetclinic.common.exception.BusinessException;
import com.vetclinic.common.exception.ErrorCode;

public class AuditLogNotFoundException extends BusinessException {

    public AuditLogNotFoundException(UUID id) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "Audit log not found with id: " + id,
                Map.of("auditLogId", id));
    }
}
