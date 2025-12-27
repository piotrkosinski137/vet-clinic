package com.vetclinic.billing.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.billing.domain.model.ItemCategory;
import com.vetclinic.billing.domain.model.PriceListItem;

public interface PriceListRepository {

    PriceListItem save(PriceListItem item);

    Optional<PriceListItem> findById(UUID id);

    List<PriceListItem> findAll();

    List<PriceListItem> findByActive(Boolean active);

    List<PriceListItem> findByCategory(ItemCategory category);

    List<PriceListItem> findByNameContainingIgnoreCase(String name);

    Optional<PriceListItem> findByCode(String code);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    List<PriceListItem> findLowStockItems();

    Optional<PriceListItem> findByBarcode(String barcode);

    List<PriceListItem> findInStockItems();
}
