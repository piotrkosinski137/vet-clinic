package com.vetclinic.compliance.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vetclinic.compliance.domain.model.DocumentVersion;

public interface JpaDocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {

    List<DocumentVersion> findByDocumentTypeAndDocumentIdOrderByVersionNumberDesc(
            String documentType, UUID documentId);

    Optional<DocumentVersion> findByDocumentTypeAndDocumentIdAndVersionNumber(
            String documentType, UUID documentId, Integer versionNumber);

    Optional<DocumentVersion> findByDocumentTypeAndDocumentIdAndIsCurrentTrue(
            String documentType, UUID documentId);

    @Query(
            "SELECT COALESCE(MAX(dv.versionNumber), 0) + 1 FROM DocumentVersion dv "
                    + "WHERE dv.documentType = :documentType AND dv.documentId = :documentId")
    Integer getNextVersionNumber(
            @Param("documentType") String documentType, @Param("documentId") UUID documentId);
}
