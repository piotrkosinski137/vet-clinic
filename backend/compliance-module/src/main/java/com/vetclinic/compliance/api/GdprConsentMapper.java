package com.vetclinic.compliance.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.vetclinic.compliance.api.dto.GdprConsentRequest;
import com.vetclinic.compliance.api.dto.GdprConsentResponse;
import com.vetclinic.compliance.domain.model.GdprConsent;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GdprConsentMapper {

    GdprConsent toEntity(GdprConsentRequest request);

    @Mapping(target = "active", expression = "java(consent.isActive())")
    GdprConsentResponse toResponse(GdprConsent consent);

    List<GdprConsentResponse> toResponseList(List<GdprConsent> consents);
}
