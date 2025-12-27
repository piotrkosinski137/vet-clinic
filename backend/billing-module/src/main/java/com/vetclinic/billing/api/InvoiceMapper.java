package com.vetclinic.billing.api;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.vetclinic.billing.api.dto.InvoiceItemDto;
import com.vetclinic.billing.api.dto.InvoicePrintResponse;
import com.vetclinic.billing.api.dto.InvoiceRequest;
import com.vetclinic.billing.api.dto.InvoiceResponse;
import com.vetclinic.billing.domain.model.Invoice;
import com.vetclinic.billing.domain.model.InvoiceItem;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvoiceMapper {

    Invoice toEntity(InvoiceRequest request);

    @Mapping(target = "remainingAmount", expression = "java(invoice.getRemainingAmount())")
    InvoiceResponse toResponse(Invoice invoice);

    List<InvoiceResponse> toResponseList(List<Invoice> invoices);

    InvoiceItem toInvoiceItem(InvoiceItemDto dto);

    InvoiceItemDto toInvoiceItemDto(InvoiceItem item);

    List<InvoiceItem> toInvoiceItemList(List<InvoiceItemDto> dtos);

    List<InvoiceItemDto> toInvoiceItemDtoList(List<InvoiceItem> items);

    @Mapping(target = "invoiceId", source = "id")
    @Mapping(target = "remainingAmount", expression = "java(invoice.getRemainingAmount())")
    @Mapping(target = "generatedAt", source = ".", qualifiedByName = "generateTimestamp")
    InvoicePrintResponse toPrintResponse(Invoice invoice);

    @Named("generateTimestamp")
    default String generateTimestamp(Invoice invoice) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
    }
}
