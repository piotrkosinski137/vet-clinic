package com.vetclinic.listener;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetclinic.billing.domain.InsufficientStockException;
import com.vetclinic.billing.domain.InventoryConsumptionException;
import com.vetclinic.billing.domain.InventoryService;
import com.vetclinic.common.event.EntityUpdatedEvent;
import com.vetclinic.common.tenant.TenantContext;
import com.vetclinic.visit.domain.VisitSnapshot;
import com.vetclinic.visit.domain.VisitSnapshot.UsedMaterialSnapshot;
import com.vetclinic.visit.domain.model.VisitPriority;
import com.vetclinic.visit.domain.model.VisitStatus;

@ExtendWith(MockitoExtension.class)
class InventoryConsumptionListenerTest {

    @Mock private InventoryService inventoryService;

    private InventoryConsumptionListener listener;

    private static final UUID VISIT_ID = UUID.randomUUID();
    private static final UUID PATIENT_ID = UUID.randomUUID();
    private static final UUID MATERIAL_ID = UUID.randomUUID();
    private static final UUID CLINIC_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        listener = new InventoryConsumptionListener(inventoryService);
        TenantContext.setCurrentClinicId(CLINIC_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldConsumeInventoryWhenVisitCompleted() {
        // given
        var material =
                new UsedMaterialSnapshot(
                        MATERIAL_ID,
                        "Test Material",
                        BigDecimal.valueOf(5),
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(20),
                        "ml");
        var oldSnapshot = createSnapshot(VisitStatus.IN_PROGRESS, List.of());
        var newSnapshot = createSnapshot(VisitStatus.COMPLETED, List.of(material));
        var event = createUpdateEvent(oldSnapshot, newSnapshot, Set.of("status"));

        // when
        listener.onVisitUpdated(event);

        // then
        verify(inventoryService).recordUsage(eq(VISIT_ID), any());
    }

    @Test
    void shouldNotConsumeIfStatusNotCompleted() {
        // given
        var oldSnapshot = createSnapshot(VisitStatus.SCHEDULED, List.of());
        var newSnapshot = createSnapshot(VisitStatus.IN_PROGRESS, List.of());
        var event = createUpdateEvent(oldSnapshot, newSnapshot, Set.of("status"));

        // when
        listener.onVisitUpdated(event);

        // then
        verify(inventoryService, never()).recordUsage(any(), any());
    }

    @Test
    void shouldNotConsumeIfAlreadyCompleted() {
        // given
        var material =
                new UsedMaterialSnapshot(
                        MATERIAL_ID,
                        "Test Material",
                        BigDecimal.valueOf(5),
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(20),
                        "ml");
        var oldSnapshot = createSnapshot(VisitStatus.COMPLETED, List.of(material));
        var newSnapshot = createSnapshot(VisitStatus.COMPLETED, List.of(material));
        var event = createUpdateEvent(oldSnapshot, newSnapshot, Set.of("status"));

        // when
        listener.onVisitUpdated(event);

        // then
        verify(inventoryService, never()).recordUsage(any(), any());
    }

    @Test
    void shouldNotConsumeIfNoMaterials() {
        // given
        var oldSnapshot = createSnapshot(VisitStatus.IN_PROGRESS, List.of());
        var newSnapshot = createSnapshot(VisitStatus.COMPLETED, List.of());
        var event = createUpdateEvent(oldSnapshot, newSnapshot, Set.of("status"));

        // when
        listener.onVisitUpdated(event);

        // then
        verify(inventoryService, never()).recordUsage(any(), any());
    }

    @Test
    void shouldNotConsumeIfAggregateTypeIsNotVisit() {
        // given
        var oldSnapshot = createSnapshot(VisitStatus.IN_PROGRESS, List.of());
        var newSnapshot = createSnapshot(VisitStatus.COMPLETED, List.of());
        var event =
                new EntityUpdatedEvent(
                        VISIT_ID,
                        "Patient", // Wrong aggregate type
                        CLINIC_ID,
                        USER_ID,
                        "Test User",
                        oldSnapshot,
                        newSnapshot,
                        Set.of("status"));

        // when
        listener.onVisitUpdated(event);

        // then
        verify(inventoryService, never()).recordUsage(any(), any());
    }

    @Test
    void shouldNotConsumeIfStatusFieldNotChanged() {
        // given
        var material =
                new UsedMaterialSnapshot(
                        MATERIAL_ID,
                        "Test Material",
                        BigDecimal.valueOf(5),
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(20),
                        "ml");
        var oldSnapshot = createSnapshot(VisitStatus.IN_PROGRESS, List.of(material));
        var newSnapshot = createSnapshot(VisitStatus.IN_PROGRESS, List.of(material));
        var event =
                createUpdateEvent(oldSnapshot, newSnapshot, Set.of("notes")); // status not changed

        // when
        listener.onVisitUpdated(event);

        // then
        verify(inventoryService, never()).recordUsage(any(), any());
    }

    @Test
    void shouldThrowWhenConsumptionFails() {
        // given
        var material =
                new UsedMaterialSnapshot(
                        MATERIAL_ID,
                        "Test Material",
                        BigDecimal.valueOf(100), // Large quantity
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(20),
                        "ml");
        var oldSnapshot = createSnapshot(VisitStatus.IN_PROGRESS, List.of());
        var newSnapshot = createSnapshot(VisitStatus.COMPLETED, List.of(material));
        var event = createUpdateEvent(oldSnapshot, newSnapshot, Set.of("status"));

        doThrow(
                        new InsufficientStockException(
                                MATERIAL_ID,
                                "Test Material",
                                BigDecimal.valueOf(100),
                                BigDecimal.valueOf(5)))
                .when(inventoryService)
                .recordUsage(eq(VISIT_ID), any());

        // when/then
        assertThatThrownBy(() -> listener.onVisitUpdated(event))
                .isInstanceOf(InventoryConsumptionException.class)
                .hasMessageContaining("Failed to consume inventory for visit");
    }

    private VisitSnapshot createSnapshot(VisitStatus status, List<UsedMaterialSnapshot> materials) {
        return new VisitSnapshot(
                VISIT_ID,
                PATIENT_ID,
                null, // clientId
                null, // veterinarianId
                null, // veterinarianName
                LocalDateTime.now(),
                30, // durationMinutes
                status,
                "Checkup", // reason
                null, // interview
                null, // examination
                null, // diagnosis
                null, // treatment
                null, // recommendations
                null, // notes
                null, // weight
                null, // temperature
                null, // nextVisitDate
                null, // checkedInAt
                null, // waitingRoomNotes
                VisitPriority.NORMAL,
                List.of(), // medications
                materials);
    }

    private EntityUpdatedEvent createUpdateEvent(
            VisitSnapshot oldSnapshot, VisitSnapshot newSnapshot, Set<String> changedFields) {
        return new EntityUpdatedEvent(
                VISIT_ID,
                "Visit",
                CLINIC_ID,
                USER_ID,
                "Test User",
                oldSnapshot,
                newSnapshot,
                changedFields);
    }
}
