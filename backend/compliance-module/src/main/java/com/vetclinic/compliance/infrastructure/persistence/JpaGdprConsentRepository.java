package com.vetclinic.compliance.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vetclinic.compliance.domain.model.ConsentStatus;
import com.vetclinic.compliance.domain.model.ConsentType;
import com.vetclinic.compliance.domain.model.GdprConsent;

public interface JpaGdprConsentRepository extends JpaRepository<GdprConsent, UUID> {

    List<GdprConsent> findByClientIdOrderByRequestedAtDesc(UUID clientId);

    List<GdprConsent> findByClientIdAndConsentTypeOrderByRequestedAtDesc(
            UUID clientId, ConsentType consentType);

    List<GdprConsent> findByStatusOrderByRequestedAtDesc(ConsentStatus status);

    @Query(
            "SELECT gc FROM GdprConsent gc WHERE gc.clientId = :clientId AND gc.consentType = :consentType "
                    + "AND gc.status = 'GRANTED' AND (gc.expiresAt IS NULL OR gc.expiresAt >= CURRENT_DATE)")
    Optional<GdprConsent> findActiveConsentByClientIdAndType(
            @Param("clientId") UUID clientId, @Param("consentType") ConsentType consentType);
}
