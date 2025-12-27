package com.vetclinic.compliance.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.vetclinic.compliance.api.dto.DocumentVersionResponse;
import com.vetclinic.compliance.domain.model.DocumentVersion;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DocumentVersionMapper {

    DocumentVersionResponse toResponse(DocumentVersion documentVersion);

    List<DocumentVersionResponse> toResponseList(List<DocumentVersion> documentVersions);
}
