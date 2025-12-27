package com.vetclinic.compliance.domain;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.vetclinic.common.exception.BusinessException;
import com.vetclinic.common.exception.ErrorCode;
import com.vetclinic.compliance.domain.model.ConsentType;

public class ConsentNotFoundException extends BusinessException {

    public ConsentNotFoundException(UUID id) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "GDPR consent not found with id: " + id,
                Map.of("consentId", id));
    }

    public ConsentNotFoundException(UUID clientId, ConsentType consentType) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "No active GDPR consent found for client: "
                        + clientId
                        + " and type: "
                        + consentType,
                Map.of("clientId", clientId, "consentType", consentType));
    }
}
