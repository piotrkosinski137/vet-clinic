package com.vetclinic.compliance.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import com.vetclinic.common.tenant.TenantAwareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gdpr_consents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdprConsent extends TenantAwareEntity {

    @Column(nullable = false)
    private UUID clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsentType consentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConsentStatus status = ConsentStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime grantedAt;

    private LocalDateTime revokedAt;

    private LocalDate expiresAt;

    @Column(columnDefinition = "TEXT")
    private String consentText;

    private String consentVersion;

    private String signatureReference;

    @Column(nullable = false)
    private String ipAddress;

    @Column(length = 500)
    private String notes;

    public boolean isActive() {
        if (status != ConsentStatus.GRANTED) {
            return false;
        }
        if (expiresAt != null && expiresAt.isBefore(LocalDate.now())) {
            return false;
        }
        return true;
    }
}
