package com.vetclinic.client.api;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.vetclinic.client.api.dto.ClientRequest;
import com.vetclinic.client.api.dto.ClientResponse;
import com.vetclinic.client.domain.model.Client;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    Client toEntity(ClientRequest request);

    ClientResponse toResponse(Client client);
}
