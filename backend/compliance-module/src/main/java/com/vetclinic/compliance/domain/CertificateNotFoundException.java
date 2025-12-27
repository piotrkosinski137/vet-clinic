package com.vetclinic.compliance.domain;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.vetclinic.common.exception.BusinessException;
import com.vetclinic.common.exception.ErrorCode;

public class CertificateNotFoundException extends BusinessException {

    public CertificateNotFoundException(UUID id) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "Vaccination certificate not found with id: " + id,
                Map.of("certificateId", id));
    }

    public CertificateNotFoundException(String certificateNumber) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "Vaccination certificate not found with number: " + certificateNumber,
                Map.of("certificateNumber", certificateNumber));
    }
}
