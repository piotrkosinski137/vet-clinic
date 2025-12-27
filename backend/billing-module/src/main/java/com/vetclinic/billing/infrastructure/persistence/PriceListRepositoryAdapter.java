package com.vetclinic.billing.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.billing.domain.model.ItemCategory;
import com.vetclinic.billing.domain.model.PriceListItem;
import com.vetclinic.billing.domain.port.PriceListRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class PriceListRepositoryAdapter implements PriceListRepository {

    private final JpaPriceListRepository jpaRepository;

    @Override
    public PriceListItem save(PriceListItem item) {
        return jpaRepository.save(item);
    }

    @Override
    public Optional<PriceListItem> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<PriceListItem> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<PriceListItem> findByActive(Boolean active) {
        return jpaRepository.findByActive(active);
    }

    @Override
    public List<PriceListItem> findByCategory(ItemCategory category) {
        return jpaRepository.findByCategory(category);
    }

    @Override
    public List<PriceListItem> findByNameContainingIgnoreCase(String name) {
        return jpaRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Optional<PriceListItem> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<PriceListItem> findLowStockItems() {
        return jpaRepository.findLowStockItems();
    }

    @Override
    public Optional<PriceListItem> findByBarcode(String barcode) {
        return jpaRepository.findByBarcode(barcode);
    }

    @Override
    public List<PriceListItem> findInStockItems() {
        return jpaRepository.findInStockItems();
    }
}
