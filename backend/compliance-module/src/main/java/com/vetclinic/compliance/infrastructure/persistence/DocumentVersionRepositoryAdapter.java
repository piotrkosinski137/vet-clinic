package com.vetclinic.compliance.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.common.tenant.TenantContext;
import com.vetclinic.compliance.domain.model.DocumentVersion;
import com.vetclinic.compliance.domain.port.DocumentVersionRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DocumentVersionRepositoryAdapter implements DocumentVersionRepository {

    private final JpaDocumentVersionRepository jpaRepository;

    @Override
    public DocumentVersion save(DocumentVersion documentVersion) {
        if (documentVersion.getClinicId() == null) {
            documentVersion.setClinicId(TenantContext.getCurrentClinicId());
        }
        return jpaRepository.save(documentVersion);
    }

    @Override
    public Optional<DocumentVersion> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<DocumentVersion> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<DocumentVersion> findByDocumentTypeAndDocumentId(
            String documentType, UUID documentId) {
        return jpaRepository.findByDocumentTypeAndDocumentIdOrderByVersionNumberDesc(
                documentType, documentId);
    }

    @Override
    public Optional<DocumentVersion> findByDocumentTypeAndDocumentIdAndVersionNumber(
            String documentType, UUID documentId, Integer versionNumber) {
        return jpaRepository.findByDocumentTypeAndDocumentIdAndVersionNumber(
                documentType, documentId, versionNumber);
    }

    @Override
    public Optional<DocumentVersion> findCurrentVersion(String documentType, UUID documentId) {
        return jpaRepository.findByDocumentTypeAndDocumentIdAndIsCurrentTrue(
                documentType, documentId);
    }

    @Override
    public Integer getNextVersionNumber(String documentType, UUID documentId) {
        return jpaRepository.getNextVersionNumber(documentType, documentId);
    }
}
