package com.vetclinic.compliance.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.vetclinic.compliance.api.dto.VaccinationCertificateRequest;
import com.vetclinic.compliance.api.dto.VaccinationCertificateResponse;
import com.vetclinic.compliance.domain.model.VaccinationCertificate;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VaccinationCertificateMapper {

    VaccinationCertificate toEntity(VaccinationCertificateRequest request);

    @Mapping(target = "expired", expression = "java(certificate.isExpired())")
    VaccinationCertificateResponse toResponse(VaccinationCertificate certificate);

    List<VaccinationCertificateResponse> toResponseList(List<VaccinationCertificate> certificates);
}
