package com.vetclinic.visit.api.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import com.vetclinic.visit.domain.model.VisitType;

/**
 * DTO for visit draft data. Used for auto-saving visit form data that hasn't been committed yet.
 */
public record VisitDraftDto(
        VisitType visitType,
        String interview,
        String examination,
        String diagnosis,
        String treatment,
        String recommendations,
        Double weight,
        Double temperature,
        LocalDateTime nextVisitDate,
        List<UsedMaterialDto> usedMaterials,
        List<MedicationDto> medications,

        // Read-only metadata (populated by server on GET)
        Instant savedAt,
        String savedBy) {

    /** Create a DTO without metadata (for client requests). */
    public static VisitDraftDto forRequest(
            VisitType visitType,
            String interview,
            String examination,
            String diagnosis,
            String treatment,
            String recommendations,
            Double weight,
            Double temperature,
            LocalDateTime nextVisitDate,
            List<UsedMaterialDto> usedMaterials,
            List<MedicationDto> medications) {
        return new VisitDraftDto(
                visitType,
                interview,
                examination,
                diagnosis,
                treatment,
                recommendations,
                weight,
                temperature,
                nextVisitDate,
                usedMaterials,
                medications,
                null,
                null);
    }
}
