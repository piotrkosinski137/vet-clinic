package com.vetclinic.compliance.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.vetclinic.common.exception.BusinessException;
import com.vetclinic.common.exception.ErrorCode;

public class DocumentVersionNotFoundException extends BusinessException {

    public DocumentVersionNotFoundException(UUID id) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "Document version not found with id: " + id,
                Map.of("documentVersionId", id));
    }

    public DocumentVersionNotFoundException(
            String documentType, UUID documentId, Integer versionNumber) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                buildMessage(documentType, documentId, versionNumber),
                buildContext(documentType, documentId, versionNumber));
    }

    private static String buildMessage(
            String documentType, UUID documentId, Integer versionNumber) {
        if (versionNumber == null) {
            return "No current version found for document type: "
                    + documentType
                    + ", documentId: "
                    + documentId;
        }
        return "Document version not found for type: "
                + documentType
                + ", documentId: "
                + documentId
                + ", version: "
                + versionNumber;
    }

    private static Map<String, Object> buildContext(
            String documentType, UUID documentId, Integer versionNumber) {
        Map<String, Object> context = new HashMap<>();
        context.put("documentType", documentType);
        context.put("documentId", documentId);
        if (versionNumber != null) {
            context.put("versionNumber", versionNumber);
        }
        return context;
    }
}
