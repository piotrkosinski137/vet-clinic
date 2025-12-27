package com.vetclinic.compliance.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.common.tenant.TenantContext;
import com.vetclinic.compliance.domain.model.ConsentStatus;
import com.vetclinic.compliance.domain.model.ConsentType;
import com.vetclinic.compliance.domain.model.GdprConsent;
import com.vetclinic.compliance.domain.port.GdprConsentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GdprConsentRepositoryAdapter implements GdprConsentRepository {

    private final JpaGdprConsentRepository jpaRepository;

    @Override
    public GdprConsent save(GdprConsent consent) {
        if (consent.getClinicId() == null) {
            consent.setClinicId(TenantContext.getCurrentClinicId());
        }
        return jpaRepository.save(consent);
    }

    @Override
    public Optional<GdprConsent> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<GdprConsent> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<GdprConsent> findByClientId(UUID clientId) {
        return jpaRepository.findByClientIdOrderByRequestedAtDesc(clientId);
    }

    @Override
    public List<GdprConsent> findByClientIdAndConsentType(UUID clientId, ConsentType consentType) {
        return jpaRepository.findByClientIdAndConsentTypeOrderByRequestedAtDesc(
                clientId, consentType);
    }

    @Override
    public List<GdprConsent> findByStatus(ConsentStatus status) {
        return jpaRepository.findByStatusOrderByRequestedAtDesc(status);
    }

    @Override
    public Optional<GdprConsent> findActiveConsentByClientIdAndType(
            UUID clientId, ConsentType consentType) {
        return jpaRepository.findActiveConsentByClientIdAndType(clientId, consentType);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
