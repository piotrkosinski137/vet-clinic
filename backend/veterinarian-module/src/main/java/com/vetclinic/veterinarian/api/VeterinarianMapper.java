package com.vetclinic.veterinarian.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.vetclinic.veterinarian.api.dto.VeterinarianRequest;
import com.vetclinic.veterinarian.api.dto.VeterinarianResponse;
import com.vetclinic.veterinarian.domain.model.Veterinarian;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VeterinarianMapper {

    Veterinarian toEntity(VeterinarianRequest request);

    VeterinarianResponse toResponse(Veterinarian veterinarian);

    List<VeterinarianResponse> toResponseList(List<Veterinarian> veterinarians);
}
