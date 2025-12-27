package com.vetclinic.compliance.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
public class DocumentVersionRequest {

    @NotBlank(message = "Document type is required")
    private String documentType;

    @NotNull(message = "Document ID is required")
    private UUID documentId;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "User name is required")
    private String userName;

    private String changeReason;
}
