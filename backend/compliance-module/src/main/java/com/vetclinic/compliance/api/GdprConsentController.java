package com.vetclinic.compliance.api;

import static com.vetclinic.common.security.Roles.CAN_MANAGE_CLIENTS;
import static com.vetclinic.common.security.Roles.HAS_ANY_ROLE;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.compliance.api.dto.GdprConsentRequest;
import com.vetclinic.compliance.api.dto.GdprConsentResponse;
import com.vetclinic.compliance.domain.ConsentService;
import com.vetclinic.compliance.domain.model.ConsentStatus;
import com.vetclinic.compliance.domain.model.ConsentType;
import com.vetclinic.compliance.domain.model.GdprConsent;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/consents")
@RequiredArgsConstructor
public class GdprConsentController {

    private final ConsentService consentService;
    private final GdprConsentMapper mapper;

    @PostMapping
    @PreAuthorize(CAN_MANAGE_CLIENTS)
    public ResponseEntity<GdprConsentResponse> createConsent(
            @Valid @RequestBody GdprConsentRequest request) {
        GdprConsent consent = mapper.toEntity(request);
        GdprConsent created = consentService.createConsent(consent);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<GdprConsentResponse>> getAllConsents() {
        List<GdprConsent> consents = consentService.getAllConsents();
        return ResponseEntity.ok(mapper.toResponseList(consents));
    }

    @GetMapping("/{id}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<GdprConsentResponse> getConsent(@PathVariable UUID id) {
        GdprConsent consent = consentService.getConsent(id);
        return ResponseEntity.ok(mapper.toResponse(consent));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<GdprConsentResponse>> getConsentsByClient(
            @PathVariable UUID clientId) {
        List<GdprConsent> consents = consentService.getConsentsByClient(clientId);
        return ResponseEntity.ok(mapper.toResponseList(consents));
    }

    @GetMapping("/client/{clientId}/type/{consentType}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<GdprConsentResponse>> getConsentsByClientAndType(
            @PathVariable UUID clientId, @PathVariable ConsentType consentType) {
        List<GdprConsent> consents =
                consentService.getConsentsByClientAndType(clientId, consentType);
        return ResponseEntity.ok(mapper.toResponseList(consents));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<GdprConsentResponse>> getConsentsByStatus(
            @PathVariable ConsentStatus status) {
        List<GdprConsent> consents = consentService.getConsentsByStatus(status);
        return ResponseEntity.ok(mapper.toResponseList(consents));
    }

    @GetMapping("/client/{clientId}/type/{consentType}/active")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<Boolean> hasActiveConsent(
            @PathVariable UUID clientId, @PathVariable ConsentType consentType) {
        boolean hasActive = consentService.hasActiveConsent(clientId, consentType);
        return ResponseEntity.ok(hasActive);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_CLIENTS)
    public ResponseEntity<GdprConsentResponse> updateConsent(
            @PathVariable UUID id, @Valid @RequestBody GdprConsentRequest request) {
        GdprConsent updatedConsent = mapper.toEntity(request);
        GdprConsent consent = consentService.updateConsent(id, updatedConsent);
        return ResponseEntity.ok(mapper.toResponse(consent));
    }

    @PutMapping("/{id}/grant")
    @PreAuthorize(CAN_MANAGE_CLIENTS)
    public ResponseEntity<GdprConsentResponse> grantConsent(
            @PathVariable UUID id, @RequestParam(required = false) String signatureReference) {
        GdprConsent consent = consentService.grantConsent(id, signatureReference);
        return ResponseEntity.ok(mapper.toResponse(consent));
    }

    @PutMapping("/{id}/revoke")
    @PreAuthorize(CAN_MANAGE_CLIENTS)
    public ResponseEntity<GdprConsentResponse> revokeConsent(
            @PathVariable UUID id, @RequestParam(required = false) String reason) {
        GdprConsent consent = consentService.revokeConsent(id, reason);
        return ResponseEntity.ok(mapper.toResponse(consent));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_CLIENTS)
    public ResponseEntity<Void> deleteConsent(@PathVariable UUID id) {
        consentService.deleteConsent(id);
        return ResponseEntity.noContent().build();
    }
}
