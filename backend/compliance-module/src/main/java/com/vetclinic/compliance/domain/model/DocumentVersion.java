package com.vetclinic.compliance.domain.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.vetclinic.common.tenant.TenantAwareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVersion extends TenantAwareEntity {

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private String createdByUserName;

    @Column(nullable = false)
    private Instant versionCreatedAt;

    @Column(length = 500)
    private String changeReason;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCurrent = true;

    private String checksum;
}
