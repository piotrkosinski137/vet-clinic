package com.vetclinic.visit.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.vetclinic.visit.api.dto.MedicationDto;
import com.vetclinic.visit.api.dto.UsedMaterialDto;
import com.vetclinic.visit.api.dto.VisitDraftDto;
import com.vetclinic.visit.domain.model.MedicationSnapshot;
import com.vetclinic.visit.domain.model.UsedMaterialSnapshot;
import com.vetclinic.visit.domain.model.VisitDraft;

/** Mapper for visit draft entities and DTOs. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VisitDraftMapper {

    /** Convert VisitDraft entity to DTO. */
    default VisitDraftDto toDto(VisitDraft draft) {
        if (draft == null) {
            return null;
        }
        return new VisitDraftDto(
                draft.getVisitType(),
                draft.getInterview(),
                draft.getExamination(),
                draft.getDiagnosis(),
                draft.getTreatment(),
                draft.getRecommendations(),
                draft.getWeight(),
                draft.getTemperature(),
                draft.getNextVisitDate(),
                toUsedMaterialDtoList(draft.getUsedMaterials()),
                toMedicationDtoList(draft.getMedications()),
                draft.getSavedAt(),
                draft.getSavedBy());
    }

    /** Convert UsedMaterialSnapshot to DTO. */
    default UsedMaterialDto toUsedMaterialDto(UsedMaterialSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new UsedMaterialDto(
                snapshot.materialId(),
                snapshot.name(),
                snapshot.quantity(),
                snapshot.costPrice(),
                snapshot.sellPrice(),
                snapshot.unit());
    }

    /** Convert DTO to UsedMaterialSnapshot. */
    default UsedMaterialSnapshot toUsedMaterialSnapshot(UsedMaterialDto dto) {
        if (dto == null) {
            return null;
        }
        return new UsedMaterialSnapshot(
                dto.materialId(),
                dto.name(),
                dto.quantity(),
                dto.costPrice(),
                dto.sellPrice(),
                dto.unit());
    }

    /** Convert MedicationSnapshot to DTO. */
    default MedicationDto toMedicationDto(MedicationSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new MedicationDto(
                snapshot.name(),
                snapshot.dosage(),
                snapshot.frequency(),
                snapshot.duration(),
                snapshot.notes());
    }

    /** Convert DTO to MedicationSnapshot. */
    default MedicationSnapshot toMedicationSnapshot(MedicationDto dto) {
        if (dto == null) {
            return null;
        }
        return new MedicationSnapshot(
                dto.name(), dto.dosage(), dto.frequency(), dto.duration(), dto.notes());
    }

    /** Convert list of UsedMaterialSnapshot to list of DTOs. */
    default List<UsedMaterialDto> toUsedMaterialDtoList(List<UsedMaterialSnapshot> snapshots) {
        if (snapshots == null) {
            return null;
        }
        return snapshots.stream().map(this::toUsedMaterialDto).toList();
    }

    /** Convert list of DTOs to list of UsedMaterialSnapshot. */
    default List<UsedMaterialSnapshot> toUsedMaterialSnapshotList(List<UsedMaterialDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream().map(this::toUsedMaterialSnapshot).toList();
    }

    /** Convert list of MedicationSnapshot to list of DTOs. */
    default List<MedicationDto> toMedicationDtoList(List<MedicationSnapshot> snapshots) {
        if (snapshots == null) {
            return null;
        }
        return snapshots.stream().map(this::toMedicationDto).toList();
    }

    /** Convert list of DTOs to list of MedicationSnapshot. */
    default List<MedicationSnapshot> toMedicationSnapshotList(List<MedicationDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream().map(this::toMedicationSnapshot).toList();
    }
}
