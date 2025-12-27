package com.vetclinic.visit.api;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.vetclinic.visit.api.dto.MedicationDto;
import com.vetclinic.visit.api.dto.UsedMaterialDto;
import com.vetclinic.visit.api.dto.VisitRequest;
import com.vetclinic.visit.api.dto.VisitResponse;
import com.vetclinic.visit.api.dto.VisitSummaryResponse;
import com.vetclinic.visit.domain.model.Medication;
import com.vetclinic.visit.domain.model.UsedMaterial;
import com.vetclinic.visit.domain.model.Visit;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VisitMapper {

    @Mapping(
            target = "visitType",
            defaultExpression = "java(com.vetclinic.visit.domain.model.VisitType.CONSULTATION)")
    Visit toEntity(VisitRequest request);

    VisitResponse toResponse(Visit visit);

    List<VisitResponse> toResponseList(List<Visit> visits);

    @Mapping(target = "visitId", source = "id")
    @Mapping(target = "generatedAt", source = ".", qualifiedByName = "generateTimestamp")
    VisitSummaryResponse toSummaryResponse(Visit visit);

    Medication toMedication(MedicationDto dto);

    MedicationDto toMedicationDto(Medication medication);

    List<Medication> toMedicationList(List<MedicationDto> dtos);

    List<MedicationDto> toMedicationDtoList(List<Medication> medications);

    UsedMaterial toUsedMaterial(UsedMaterialDto dto);

    UsedMaterialDto toUsedMaterialDto(UsedMaterial material);

    List<UsedMaterial> toUsedMaterialList(List<UsedMaterialDto> dtos);

    List<UsedMaterialDto> toUsedMaterialDtoList(List<UsedMaterial> materials);

    @Named("generateTimestamp")
    default String generateTimestamp(Visit visit) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
    }
}
