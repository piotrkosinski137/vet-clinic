package com.vetclinic.visit.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Temporary storage for visit form data before final save. Enables auto-save functionality so
 * doctors can close the modal, move to another workstation, and continue where they left off.
 */
@Entity
@Table(name = "visit_drafts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitDraft {

    @Id
    @Column(name = "visit_id")
    private UUID visitId;

    @Column(name = "clinic_id", nullable = false)
    private UUID clinicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type")
    private VisitType visitType;

    @Column(columnDefinition = "TEXT")
    private String interview;

    @Column(columnDefinition = "TEXT")
    private String examination;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    private Double weight;

    private Double temperature;

    @Column(name = "next_visit_date")
    private LocalDate nextVisitDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "used_materials", columnDefinition = "jsonb")
    private List<UsedMaterialSnapshot> usedMaterials;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "medications", columnDefinition = "jsonb")
    private List<MedicationSnapshot> medications;

    @Column(name = "saved_at", nullable = false)
    private Instant savedAt;

    @Column(name = "saved_by")
    private String savedBy;
}
