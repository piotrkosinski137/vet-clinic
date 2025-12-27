package com.vetclinic.compliance.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.compliance.domain.model.DocumentVersion;

public interface DocumentVersionRepository {

    DocumentVersion save(DocumentVersion documentVersion);

    Optional<DocumentVersion> findById(UUID id);

    List<DocumentVersion> findAll();

    List<DocumentVersion> findByDocumentTypeAndDocumentId(String documentType, UUID documentId);

    Optional<DocumentVersion> findByDocumentTypeAndDocumentIdAndVersionNumber(
            String documentType, UUID documentId, Integer versionNumber);

    Optional<DocumentVersion> findCurrentVersion(String documentType, UUID documentId);

    Integer getNextVersionNumber(String documentType, UUID documentId);
}
