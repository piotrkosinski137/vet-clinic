package com.vetclinic.compliance.api;

import static com.vetclinic.common.security.Roles.CAN_VIEW_AUDIT;
import static com.vetclinic.common.security.Roles.HAS_ADMIN;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.compliance.api.dto.DocumentVersionRequest;
import com.vetclinic.compliance.api.dto.DocumentVersionResponse;
import com.vetclinic.compliance.domain.DocumentVersionService;
import com.vetclinic.compliance.domain.model.DocumentVersion;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/document-versions")
@RequiredArgsConstructor
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;
    private final DocumentVersionMapper mapper;

    @PostMapping
    @PreAuthorize(HAS_ADMIN)
    public ResponseEntity<DocumentVersionResponse> createVersion(
            @Valid @RequestBody DocumentVersionRequest request) {
        DocumentVersion created =
                documentVersionService.createVersion(
                        request.getDocumentType(),
                        request.getDocumentId(),
                        request.getContent(),
                        request.getUserId(),
                        request.getUserName(),
                        request.getChangeReason());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    @PreAuthorize(CAN_VIEW_AUDIT)
    public ResponseEntity<List<DocumentVersionResponse>> getAllVersions() {
        List<DocumentVersion> versions = documentVersionService.getAllVersions();
        return ResponseEntity.ok(mapper.toResponseList(versions));
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_VIEW_AUDIT)
    public ResponseEntity<DocumentVersionResponse> getVersion(@PathVariable UUID id) {
        DocumentVersion version = documentVersionService.getVersion(id);
        return ResponseEntity.ok(mapper.toResponse(version));
    }

    @GetMapping("/document/{documentType}/{documentId}")
    @PreAuthorize(CAN_VIEW_AUDIT)
    public ResponseEntity<List<DocumentVersionResponse>> getVersionHistory(
            @PathVariable String documentType, @PathVariable UUID documentId) {
        List<DocumentVersion> versions =
                documentVersionService.getVersionHistory(documentType, documentId);
        return ResponseEntity.ok(mapper.toResponseList(versions));
    }

    @GetMapping("/document/{documentType}/{documentId}/version/{versionNumber}")
    @PreAuthorize(CAN_VIEW_AUDIT)
    public ResponseEntity<DocumentVersionResponse> getSpecificVersion(
            @PathVariable String documentType,
            @PathVariable UUID documentId,
            @PathVariable Integer versionNumber) {
        DocumentVersion version =
                documentVersionService.getSpecificVersion(documentType, documentId, versionNumber);
        return ResponseEntity.ok(mapper.toResponse(version));
    }

    @GetMapping("/document/{documentType}/{documentId}/current")
    @PreAuthorize(CAN_VIEW_AUDIT)
    public ResponseEntity<DocumentVersionResponse> getCurrentVersion(
            @PathVariable String documentType, @PathVariable UUID documentId) {
        DocumentVersion version =
                documentVersionService.getCurrentVersion(documentType, documentId);
        return ResponseEntity.ok(mapper.toResponse(version));
    }
}
