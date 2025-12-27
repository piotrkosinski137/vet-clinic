package com.vetclinic.billing.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.vetclinic.billing.api.dto.PriceListItemRequest;
import com.vetclinic.billing.api.dto.PriceListItemResponse;
import com.vetclinic.billing.domain.model.PriceListItem;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PriceListMapper {

    PriceListItem toEntity(PriceListItemRequest request);

    PriceListItemResponse toResponse(PriceListItem item);

    List<PriceListItemResponse> toResponseList(List<PriceListItem> items);
}
