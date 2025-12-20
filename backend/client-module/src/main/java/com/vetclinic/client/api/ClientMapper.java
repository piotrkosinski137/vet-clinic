package com.vetclinic.client.api;

import com.vetclinic.client.api.dto.ClientRequest;
import com.vetclinic.client.api.dto.ClientResponse;
import com.vetclinic.client.domain.model.Client;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    Client toEntity(ClientRequest request);

    ClientResponse toResponse(Client client);
}
