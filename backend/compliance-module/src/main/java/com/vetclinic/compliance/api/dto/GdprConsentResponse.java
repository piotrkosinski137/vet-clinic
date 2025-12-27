package com.vetclinic.compliance.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.vetclinic.compliance.domain.model.ConsentStatus;
import com.vetclinic.compliance.domain.model.ConsentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdprConsentResponse {

    private UUID id;
    private UUID clientId;
    private ConsentType consentType;
    private ConsentStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime grantedAt;
    private LocalDateTime revokedAt;
    private LocalDate expiresAt;
    private String consentText;
    private String consentVersion;
    private String signatureReference;
    private String ipAddress;
    private String notes;
    private boolean active;
}
