package com.vetclinic.compliance.domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.compliance.domain.model.DocumentVersion;
import com.vetclinic.compliance.domain.port.DocumentVersionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;

    @Transactional
    public DocumentVersion createVersion(
            String documentType,
            UUID documentId,
            String content,
            UUID userId,
            String userName,
            String changeReason) {
        // Mark previous current version as not current
        documentVersionRepository
                .findCurrentVersion(documentType, documentId)
                .ifPresent(
                        currentVersion -> {
                            currentVersion.setIsCurrent(false);
                            documentVersionRepository.save(currentVersion);
                        });

        Integer nextVersionNumber =
                documentVersionRepository.getNextVersionNumber(documentType, documentId);

        DocumentVersion newVersion =
                DocumentVersion.builder()
                        .documentType(documentType)
                        .documentId(documentId)
                        .versionNumber(nextVersionNumber)
                        .content(content)
                        .createdByUserId(userId)
                        .createdByUserName(userName)
                        .versionCreatedAt(Instant.now())
                        .changeReason(changeReason)
                        .isCurrent(true)
                        .checksum(calculateChecksum(content))
                        .build();

        return documentVersionRepository.save(newVersion);
    }

    public DocumentVersion getVersion(UUID id) {
        return documentVersionRepository
                .findById(id)
                .orElseThrow(() -> new DocumentVersionNotFoundException(id));
    }

    public List<DocumentVersion> getAllVersions() {
        return documentVersionRepository.findAll();
    }

    public List<DocumentVersion> getVersionHistory(String documentType, UUID documentId) {
        return documentVersionRepository.findByDocumentTypeAndDocumentId(documentType, documentId);
    }

    public DocumentVersion getSpecificVersion(
            String documentType, UUID documentId, Integer versionNumber) {
        return documentVersionRepository
                .findByDocumentTypeAndDocumentIdAndVersionNumber(
                        documentType, documentId, versionNumber)
                .orElseThrow(
                        () ->
                                new DocumentVersionNotFoundException(
                                        documentType, documentId, versionNumber));
    }

    public DocumentVersion getCurrentVersion(String documentType, UUID documentId) {
        return documentVersionRepository
                .findCurrentVersion(documentType, documentId)
                .orElseThrow(
                        () -> new DocumentVersionNotFoundException(documentType, documentId, null));
    }

    private String calculateChecksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
