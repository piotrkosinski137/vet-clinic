package com.vetclinic.listener;

import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.vetclinic.billing.domain.InventoryConsumptionException;
import com.vetclinic.billing.domain.InventoryService;
import com.vetclinic.billing.domain.model.UsedMaterial;
import com.vetclinic.common.event.EntityUpdatedEvent;
import com.vetclinic.visit.domain.VisitSnapshot;
import com.vetclinic.visit.domain.model.VisitStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listens for visit completion events and triggers FIFO inventory consumption for used materials.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryConsumptionListener {

    private final InventoryService inventoryService;

    @EventListener
    public void onVisitUpdated(EntityUpdatedEvent event) {
        if (!"Visit".equals(event.getAggregateType())) {
            return;
        }

        // Check if this is a status change to COMPLETED
        if (!event.getChangedFields().contains("status")) {
            return;
        }

        var newSnapshot = (VisitSnapshot) event.getNewEntity();
        if (newSnapshot.status() != VisitStatus.COMPLETED) {
            return;
        }

        var oldSnapshot = (VisitSnapshot) event.getOldEntity();
        if (oldSnapshot.status() == VisitStatus.COMPLETED) {
            return; // Already completed, no need to consume again
        }

        log.info(
                "Visit {} completed, processing inventory consumption for {} materials",
                event.getAggregateId(),
                newSnapshot.usedMaterials() != null ? newSnapshot.usedMaterials().size() : 0);

        // Convert visit's UsedMaterials to billing module's UsedMaterials and consume
        if (newSnapshot.usedMaterials() != null && !newSnapshot.usedMaterials().isEmpty()) {
            var billingMaterials =
                    newSnapshot.usedMaterials().stream()
                            .map(
                                    m ->
                                            UsedMaterial.builder()
                                                    .materialId(m.materialId())
                                                    .quantity(m.quantity())
                                                    .notes(
                                                            "Used in visit: "
                                                                    + event.getAggregateId())
                                                    .build())
                            .collect(Collectors.toList());

            try {
                inventoryService.recordUsage(event.getAggregateId(), billingMaterials);
                log.info(
                        "Successfully consumed inventory for visit {}: {} materials",
                        event.getAggregateId(),
                        billingMaterials.size());
            } catch (Exception e) {
                log.error(
                        "Failed to consume inventory for visit {}: {}",
                        event.getAggregateId(),
                        e.getMessage(),
                        e);
                // Rethrow to fail the transaction - ensures data consistency
                // Visit completion will be rolled back if inventory cannot be consumed
                throw new InventoryConsumptionException(
                        event.getAggregateId(),
                        "Failed to consume inventory for visit: " + e.getMessage(),
                        e);
            }
        }
    }
}
