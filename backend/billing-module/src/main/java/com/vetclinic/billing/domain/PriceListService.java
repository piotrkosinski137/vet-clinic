package com.vetclinic.billing.domain;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.billing.domain.model.ItemCategory;
import com.vetclinic.billing.domain.model.PriceListItem;
import com.vetclinic.billing.domain.port.PriceListRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceListService {

    private final PriceListRepository priceListRepository;

    @Transactional
    public PriceListItem createItem(PriceListItem item) {
        return priceListRepository.save(item);
    }

    public PriceListItem getItem(UUID id) {
        return priceListRepository
                .findById(id)
                .orElseThrow(() -> new PriceListItemNotFoundException(id));
    }

    public List<PriceListItem> getAllItems() {
        return priceListRepository.findAll();
    }

    public List<PriceListItem> getActiveItems() {
        return priceListRepository.findByActive(true);
    }

    public List<PriceListItem> getItemsByCategory(ItemCategory category) {
        return priceListRepository.findByCategory(category);
    }

    public List<PriceListItem> searchByName(String name) {
        return priceListRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public PriceListItem updateItem(UUID id, PriceListItem updated) {
        PriceListItem existing = getItem(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setCostPrice(updated.getCostPrice());
        existing.setSellPrice(updated.getSellPrice());
        existing.setUnit(updated.getUnit());
        existing.setActive(updated.getActive());
        existing.setCode(updated.getCode());
        return priceListRepository.save(existing);
    }

    @Transactional
    public PriceListItem toggleActive(UUID id, Boolean active) {
        PriceListItem item = getItem(id);
        item.setActive(active);
        return priceListRepository.save(item);
    }

    @Transactional
    public void deleteItem(UUID id) {
        if (!priceListRepository.existsById(id)) {
            throw new PriceListItemNotFoundException(id);
        }
        priceListRepository.deleteById(id);
    }
}
