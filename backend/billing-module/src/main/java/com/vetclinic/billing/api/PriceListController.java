package com.vetclinic.billing.api;

import static com.vetclinic.common.security.Roles.CAN_MANAGE_INVENTORY;
import static com.vetclinic.common.security.Roles.HAS_ANY_ROLE;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.billing.api.dto.PriceListItemRequest;
import com.vetclinic.billing.api.dto.PriceListItemResponse;
import com.vetclinic.billing.domain.PriceListService;
import com.vetclinic.billing.domain.model.ItemCategory;
import com.vetclinic.billing.domain.model.PriceListItem;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/price-list")
@RequiredArgsConstructor
public class PriceListController {

    private final PriceListService priceListService;
    private final PriceListMapper priceListMapper;

    @PostMapping
    @PreAuthorize(CAN_MANAGE_INVENTORY)
    public ResponseEntity<PriceListItemResponse> createItem(
            @Valid @RequestBody PriceListItemRequest request) {
        PriceListItem item = priceListMapper.toEntity(request);
        PriceListItem saved = priceListService.createItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(priceListMapper.toResponse(saved));
    }

    @GetMapping
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<PriceListItemResponse>> getAllItems(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) ItemCategory category,
            @RequestParam(required = false) String name) {
        List<PriceListItem> items;

        if (name != null && !name.isBlank()) {
            items = priceListService.searchByName(name);
        } else if (category != null) {
            items = priceListService.getItemsByCategory(category);
        } else if (active != null) {
            items = active ? priceListService.getActiveItems() : priceListService.getAllItems();
        } else {
            items = priceListService.getAllItems();
        }

        return ResponseEntity.ok(priceListMapper.toResponseList(items));
    }

    @GetMapping("/{id}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<PriceListItemResponse> getItem(@PathVariable UUID id) {
        PriceListItem item = priceListService.getItem(id);
        return ResponseEntity.ok(priceListMapper.toResponse(item));
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_INVENTORY)
    public ResponseEntity<PriceListItemResponse> updateItem(
            @PathVariable UUID id, @Valid @RequestBody PriceListItemRequest request) {
        PriceListItem updated = priceListMapper.toEntity(request);
        PriceListItem saved = priceListService.updateItem(id, updated);
        return ResponseEntity.ok(priceListMapper.toResponse(saved));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize(CAN_MANAGE_INVENTORY)
    public ResponseEntity<PriceListItemResponse> toggleActive(
            @PathVariable UUID id, @RequestParam Boolean active) {
        PriceListItem item = priceListService.toggleActive(id, active);
        return ResponseEntity.ok(priceListMapper.toResponse(item));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_INVENTORY)
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        priceListService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
