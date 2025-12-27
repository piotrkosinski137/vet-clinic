package com.vetclinic.compliance.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.compliance.domain.model.ConsentStatus;
import com.vetclinic.compliance.domain.model.ConsentType;
import com.vetclinic.compliance.domain.model.GdprConsent;

public interface GdprConsentRepository {

    GdprConsent save(GdprConsent consent);

    Optional<GdprConsent> findById(UUID id);

    List<GdprConsent> findAll();

    List<GdprConsent> findByClientId(UUID clientId);

    List<GdprConsent> findByClientIdAndConsentType(UUID clientId, ConsentType consentType);

    List<GdprConsent> findByStatus(ConsentStatus status);

    Optional<GdprConsent> findActiveConsentByClientIdAndType(
            UUID clientId, ConsentType consentType);

    void deleteById(UUID id);
}
