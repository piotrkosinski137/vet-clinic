package com.vetclinic.compliance.api.dto;

import java.time.Instant;
import java.util.UUID;

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
public class DocumentVersionResponse {

    private UUID id;
    private String documentType;
    private UUID documentId;
    private Integer versionNumber;
    private String content;
    private UUID createdByUserId;
    private String createdByUserName;
    private Instant versionCreatedAt;
    private String changeReason;
    private Boolean isCurrent;
    private String checksum;
}
