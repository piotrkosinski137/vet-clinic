package com.vetclinic.compliance.api.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
public class GdprConsentRequest {

    @NotNull(message = "Client ID is required")
    private UUID clientId;

    @NotNull(message = "Consent type is required")
    private ConsentType consentType;

    @NotBlank(message = "Consent text is required")
    private String consentText;

    private String consentVersion;

    private LocalDate expiresAt;

    @NotBlank(message = "IP address is required")
    private String ipAddress;

    private String notes;
}
