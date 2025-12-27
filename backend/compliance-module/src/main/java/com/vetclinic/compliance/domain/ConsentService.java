package com.vetclinic.compliance.domain;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.compliance.domain.model.ConsentStatus;
import com.vetclinic.compliance.domain.model.ConsentType;
import com.vetclinic.compliance.domain.model.GdprConsent;
import com.vetclinic.compliance.domain.port.GdprConsentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsentService {

    private final GdprConsentRepository consentRepository;
    private final Clock clock;

    @Transactional
    public GdprConsent createConsent(GdprConsent consent) {
        if (consent.getRequestedAt() == null) {
            consent.setRequestedAt(LocalDateTime.now(clock));
        }
        if (consent.getStatus() == null) {
            consent.setStatus(ConsentStatus.PENDING);
        }
        return consentRepository.save(consent);
    }

    @Transactional
    public GdprConsent grantConsent(UUID consentId, String signatureReference) {
        GdprConsent consent = getConsent(consentId);
        consent.setStatus(ConsentStatus.GRANTED);
        consent.setGrantedAt(LocalDateTime.now(clock));
        consent.setSignatureReference(signatureReference);
        return consentRepository.save(consent);
    }

    @Transactional
    public GdprConsent revokeConsent(UUID consentId, String reason) {
        GdprConsent consent = getConsent(consentId);
        consent.setStatus(ConsentStatus.REVOKED);
        consent.setRevokedAt(LocalDateTime.now(clock));
        consent.setNotes(reason);
        return consentRepository.save(consent);
    }

    @Transactional
    public GdprConsent updateConsent(UUID id, GdprConsent updatedConsent) {
        GdprConsent consent = getConsent(id);
        consent.setConsentText(updatedConsent.getConsentText());
        consent.setConsentVersion(updatedConsent.getConsentVersion());
        consent.setExpiresAt(updatedConsent.getExpiresAt());
        consent.setNotes(updatedConsent.getNotes());
        return consentRepository.save(consent);
    }

    public GdprConsent getConsent(UUID id) {
        return consentRepository.findById(id).orElseThrow(() -> new ConsentNotFoundException(id));
    }

    public List<GdprConsent> getAllConsents() {
        return consentRepository.findAll();
    }

    public List<GdprConsent> getConsentsByClient(UUID clientId) {
        return consentRepository.findByClientId(clientId);
    }

    public List<GdprConsent> getConsentsByClientAndType(UUID clientId, ConsentType consentType) {
        return consentRepository.findByClientIdAndConsentType(clientId, consentType);
    }

    public List<GdprConsent> getConsentsByStatus(ConsentStatus status) {
        return consentRepository.findByStatus(status);
    }

    public boolean hasActiveConsent(UUID clientId, ConsentType consentType) {
        return consentRepository
                .findActiveConsentByClientIdAndType(clientId, consentType)
                .isPresent();
    }

    public GdprConsent getActiveConsent(UUID clientId, ConsentType consentType) {
        return consentRepository
                .findActiveConsentByClientIdAndType(clientId, consentType)
                .orElseThrow(() -> new ConsentNotFoundException(clientId, consentType));
    }

    @Transactional
    public void deleteConsent(UUID id) {
        if (consentRepository.findById(id).isEmpty()) {
            throw new ConsentNotFoundException(id);
        }
        consentRepository.deleteById(id);
    }
}
