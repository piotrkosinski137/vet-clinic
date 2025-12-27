package com.vetclinic.billing.api;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.vetclinic.billing.api.dto.InventoryItemResponse;
import com.vetclinic.billing.api.dto.InventoryTransactionResponse;
import com.vetclinic.billing.api.dto.SupplierInvoiceItemResponse;
import com.vetclinic.billing.api.dto.SupplierInvoiceResponse;
import com.vetclinic.billing.domain.model.InventoryTransaction;
import com.vetclinic.billing.domain.model.PriceListItem;
import com.vetclinic.billing.domain.model.SupplierInvoice;
import com.vetclinic.billing.domain.model.SupplierInvoiceItem;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryMapper {

    @Mapping(target = "isLowStock", expression = "java(item.isLowStock())")
    @Mapping(target = "isExpired", expression = "java(item.isExpired())")
    @Mapping(target = "isInStock", expression = "java(item.isInStock())")
    InventoryItemResponse toInventoryItemResponse(PriceListItem item);

    List<InventoryItemResponse> toInventoryItemResponseList(List<PriceListItem> items);

    @Mapping(target = "itemName", ignore = true)
    InventoryTransactionResponse toTransactionResponse(InventoryTransaction transaction);

    List<InventoryTransactionResponse> toTransactionResponseList(
            List<InventoryTransaction> transactions);

    @Mapping(target = "items", source = "items")
    SupplierInvoiceResponse toSupplierInvoiceResponse(SupplierInvoice invoice);

    List<SupplierInvoiceResponse> toSupplierInvoiceResponseList(List<SupplierInvoice> invoices);

    @Mapping(target = "itemId", source = "itemId")
    @Mapping(target = "itemName", source = "productName")
    SupplierInvoiceItemResponse toSupplierInvoiceItemResponse(SupplierInvoiceItem item);

    List<SupplierInvoiceItemResponse> toSupplierInvoiceItemResponseList(
            List<SupplierInvoiceItem> items);
}
