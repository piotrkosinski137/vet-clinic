package com.vetclinic.billing.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.vetclinic.billing.domain.model.ItemCategory;
import com.vetclinic.billing.domain.model.PriceListItem;

public interface JpaPriceListRepository extends JpaRepository<PriceListItem, UUID> {

    List<PriceListItem> findByActive(Boolean active);

    List<PriceListItem> findByCategory(ItemCategory category);

    List<PriceListItem> findByNameContainingIgnoreCase(String name);

    Optional<PriceListItem> findByCode(String code);

    @Query("SELECT p FROM PriceListItem p WHERE p.stockQuantity <= p.reorderPoint")
    List<PriceListItem> findLowStockItems();

    Optional<PriceListItem> findByBarcode(String barcode);

    @Query("SELECT p FROM PriceListItem p WHERE p.stockQuantity > 0")
    List<PriceListItem> findInStockItems();
}
