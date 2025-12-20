package com.vetclinic.patient.api;

import com.vetclinic.patient.api.dto.PatientRequest;
import com.vetclinic.patient.api.dto.PatientResponse;
import com.vetclinic.patient.domain.model.Patient;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PatientMapper {

    Patient toEntity(PatientRequest request);

    PatientResponse toResponse(Patient patient);
}
