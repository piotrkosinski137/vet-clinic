package com.vetclinic.billing.domain;

import static com.vetclinic.billing.fixtures.InventoryBatchFixture.aBatch;
import static com.vetclinic.billing.fixtures.InventoryBatchFixture.aCompleteBatch;
import static com.vetclinic.billing.fixtures.InventoryBatchFixture.aDepletedBatch;
import static com.vetclinic.billing.fixtures.InventoryBatchFixture.aPendingBatch;
import static com.vetclinic.billing.fixtures.InventoryBatchFixture.anExpiringSoonBatch;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetclinic.billing.domain.model.BatchStatus;
import com.vetclinic.billing.domain.model.InventoryBatch;
import com.vetclinic.billing.domain.model.InventoryTransaction;
import com.vetclinic.billing.domain.model.PriceListItem;
import com.vetclinic.billing.domain.model.SupplierInvoiceItem;
import com.vetclinic.billing.domain.model.TransactionType;
import com.vetclinic.billing.domain.port.InventoryBatchRepository;
import com.vetclinic.billing.domain.port.InventoryTransactionRepository;
import com.vetclinic.billing.domain.port.PriceListRepository;
import com.vetclinic.common.tenant.TenantContext;

@ExtendWith(MockitoExtension.class)
class InventoryBatchServiceTest {

    @Mock private InventoryBatchRepository batchRepository;
    @Mock private PriceListRepository priceListRepository;
    @Mock private InventoryTransactionRepository transactionRepository;

    private InventoryBatchService batchService;

    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final UUID VISIT_ID = UUID.randomUUID();
    private static final UUID CLINIC_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        batchService =
                new InventoryBatchService(
                        batchRepository, priceListRepository, transactionRepository);
        TenantContext.setCurrentClinicId(CLINIC_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldConsumeFIFOByEarliestExpiration() {
        // given
        var batch1 =
                createBatch(ITEM_ID, 10, "LOT-001", LocalDate.now().plusDays(10)); // Expires first
        var batch2 =
                createBatch(ITEM_ID, 10, "LOT-002", LocalDate.now().plusDays(30)); // Expires later
        var batch3 =
                createBatch(ITEM_ID, 10, "LOT-003", LocalDate.now().plusDays(60)); // Expires last

        given(batchRepository.findForFifoConsumption(ITEM_ID))
                .willReturn(List.of(batch1, batch2, batch3));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.valueOf(25));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        // when - consume 15 units
        var consumed =
                batchService.consumeStock(ITEM_ID, BigDecimal.valueOf(15), VISIT_ID, "VISIT");

        // then - should consume from earliest expiring first
        assertThat(consumed).hasSize(2);
        assertThat(batch1.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO); // All 10 consumed
        assertThat(batch2.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(5)); // 5 consumed
        assertThat(batch3.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(10)); // Untouched
    }

    @Test
    void shouldConsumeFIFOFromMultipleBatches() {
        // given
        var batch1 = createBatch(ITEM_ID, 5, "LOT-001", LocalDate.now().plusDays(10));
        var batch2 = createBatch(ITEM_ID, 5, "LOT-002", LocalDate.now().plusDays(20));
        var batch3 = createBatch(ITEM_ID, 5, "LOT-003", LocalDate.now().plusDays(30));

        given(batchRepository.findForFifoConsumption(ITEM_ID))
                .willReturn(List.of(batch1, batch2, batch3));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.valueOf(5));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        // when - consume all 15 units
        var consumed =
                batchService.consumeStock(ITEM_ID, BigDecimal.valueOf(15), VISIT_ID, "VISIT");

        // then - all batches consumed
        assertThat(consumed).hasSize(3);
        assertThat(batch1.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(batch1.getStatus()).isEqualTo(BatchStatus.DEPLETED);
        assertThat(batch2.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(batch2.getStatus()).isEqualTo(BatchStatus.DEPLETED);
        assertThat(batch3.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(batch3.getStatus()).isEqualTo(BatchStatus.DEPLETED);
    }

    @Test
    void shouldThrowInsufficientStockException() {
        // given
        var batch1 = createBatch(ITEM_ID, 5, "LOT-001", LocalDate.now().plusDays(10));
        var item = createItem(ITEM_ID);

        given(batchRepository.findForFifoConsumption(ITEM_ID)).willReturn(List.of(batch1));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(item));

        // when/then - trying to consume more than available
        assertThatThrownBy(
                        () ->
                                batchService.consumeStock(
                                        ITEM_ID, BigDecimal.valueOf(10), VISIT_ID, "VISIT"))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock")
                .hasMessageContaining("requested 10")
                .hasMessageContaining("available 5");
    }

    @Test
    void shouldCreateCompleteBatchWhenLotProvided() {
        // given
        var invoiceItem =
                SupplierInvoiceItem.builder()
                        .productName("Test Product")
                        .quantity(50)
                        .netPrice(BigDecimal.valueOf(10.00))
                        .batchNumber("LOT-2024-001")
                        .expirationDate(LocalDate.now().plusMonths(12))
                        .build();

        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        var batch = batchService.createBatchFromInvoiceItem(invoiceItem, ITEM_ID);

        // then
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.COMPLETE);
        assertThat(batch.getLotNumber()).isEqualTo("LOT-2024-001");
        assertThat(batch.getExpirationDate()).isEqualTo(LocalDate.now().plusMonths(12));
        assertThat(batch.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void shouldCreatePendingBatchWhenLotMissing() {
        // given
        var invoiceItem =
                SupplierInvoiceItem.builder()
                        .productName("Test Product")
                        .quantity(50)
                        .netPrice(BigDecimal.valueOf(10.00))
                        .batchNumber(null) // No LOT number
                        .expirationDate(null)
                        .build();

        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        var batch = batchService.createBatchFromInvoiceItem(invoiceItem, ITEM_ID);

        // then
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.PENDING);
        assertThat(batch.getLotNumber()).isNull();
    }

    @Test
    void shouldCompletePendingBatch() {
        // given
        var batchId = UUID.randomUUID();
        var batch =
                InventoryBatch.builder()
                        .itemId(ITEM_ID)
                        .quantity(BigDecimal.valueOf(50))
                        .status(BatchStatus.PENDING)
                        .build();
        setId(batch, batchId);

        given(batchRepository.findById(batchId)).willReturn(Optional.of(batch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        var lotNumber = "LOT-2024-999";
        var expirationDate = LocalDate.now().plusMonths(6);
        var completed = batchService.completePendingBatch(batchId, lotNumber, expirationDate);

        // then
        assertThat(completed.getStatus()).isEqualTo(BatchStatus.COMPLETE);
        assertThat(completed.getLotNumber()).isEqualTo(lotNumber);
        assertThat(completed.getExpirationDate()).isEqualTo(expirationDate);
    }

    @Test
    void shouldThrowWhenCompletingNonPendingBatch() {
        // given
        var batchId = UUID.randomUUID();
        var batch =
                InventoryBatch.builder()
                        .itemId(ITEM_ID)
                        .quantity(BigDecimal.valueOf(50))
                        .status(BatchStatus.COMPLETE) // Already complete
                        .lotNumber("LOT-001")
                        .build();
        setId(batch, batchId);

        given(batchRepository.findById(batchId)).willReturn(Optional.of(batch));

        // when/then
        assertThatThrownBy(
                        () ->
                                batchService.completePendingBatch(
                                        batchId, "NEW-LOT", LocalDate.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not pending");
    }

    @Test
    void shouldDisposeBatchAndCreateTransaction() {
        // given
        var batchId = UUID.randomUUID();
        var batch =
                InventoryBatch.builder()
                        .itemId(ITEM_ID)
                        .quantity(BigDecimal.valueOf(20))
                        .status(BatchStatus.COMPLETE)
                        .lotNumber("LOT-001")
                        .expirationDate(LocalDate.now().minusDays(5)) // Expired
                        .unitCost(BigDecimal.valueOf(5.00))
                        .build();
        setId(batch, batchId);

        given(batchRepository.findById(batchId)).willReturn(Optional.of(batch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.ZERO);
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        // when
        var disposed =
                batchService.disposeBatch(batchId, BigDecimal.valueOf(20), "Expired batch LOT-001");

        // then
        assertThat(disposed.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(disposed.getStatus()).isEqualTo(BatchStatus.DEPLETED);

        // Verify transaction was created
        verify(transactionRepository).save(any());
    }

    @Test
    void shouldPartiallyDisposeBatch() {
        // given
        var batchId = UUID.randomUUID();
        var batch =
                InventoryBatch.builder()
                        .itemId(ITEM_ID)
                        .quantity(BigDecimal.valueOf(50))
                        .status(BatchStatus.COMPLETE)
                        .lotNumber("LOT-001")
                        .unitCost(BigDecimal.valueOf(5.00))
                        .build();
        setId(batch, batchId);

        given(batchRepository.findById(batchId)).willReturn(Optional.of(batch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.valueOf(30));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        // when - dispose only 20 of 50
        var disposed =
                batchService.disposeBatch(batchId, BigDecimal.valueOf(20), "Partial disposal");

        // then
        assertThat(disposed.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(30)); // 50 - 20
        assertThat(disposed.getStatus()).isEqualTo(BatchStatus.COMPLETE); // Still has stock
    }

    @Test
    void shouldThrowWhenDisposingMoreThanAvailable() {
        // given
        var batchId = UUID.randomUUID();
        var batch =
                InventoryBatch.builder()
                        .itemId(ITEM_ID)
                        .quantity(BigDecimal.valueOf(10))
                        .status(BatchStatus.COMPLETE)
                        .lotNumber("LOT-001")
                        .build();
        setId(batch, batchId);

        given(batchRepository.findById(batchId)).willReturn(Optional.of(batch));

        // when/then
        assertThatThrownBy(
                        () ->
                                batchService.disposeBatch(
                                        batchId, BigDecimal.valueOf(20), "Too much"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot dispose 20");
    }

    // ==================== Retrieval Methods (5 tests) ====================

    @Test
    void shouldGetBatchesByStatus() {
        // given
        var pendingBatch1 = aPendingBatch().withItemId(ITEM_ID).buildWithRandomId();
        var pendingBatch2 = aPendingBatch().withItemId(ITEM_ID).buildWithRandomId();

        given(batchRepository.findByClinicIdAndStatus(CLINIC_ID, BatchStatus.PENDING))
                .willReturn(List.of(pendingBatch1, pendingBatch2));

        // when
        var batches = batchService.getBatchesByStatus(BatchStatus.PENDING);

        // then
        assertThat(batches).hasSize(2);
        assertThat(batches).allMatch(b -> b.getStatus() == BatchStatus.PENDING);
        verify(batchRepository).findByClinicIdAndStatus(CLINIC_ID, BatchStatus.PENDING);
    }

    @Test
    void shouldGetBatchesByItemId() {
        // given
        var batch1 = aCompleteBatch().withItemId(ITEM_ID).buildWithRandomId();
        var batch2 = aCompleteBatch().withItemId(ITEM_ID).buildWithRandomId();

        given(batchRepository.findByItemId(ITEM_ID)).willReturn(List.of(batch1, batch2));

        // when
        var batches = batchService.getBatchesByItemId(ITEM_ID);

        // then
        assertThat(batches).hasSize(2);
        assertThat(batches).allMatch(b -> b.getItemId().equals(ITEM_ID));
        verify(batchRepository).findByItemId(ITEM_ID);
    }

    @Test
    void shouldGetExpiringSoonBatches() {
        // given
        var expiringSoon1 = anExpiringSoonBatch(15).withItemId(ITEM_ID).buildWithRandomId();
        var expiringSoon2 = anExpiringSoonBatch(25).withItemId(ITEM_ID).buildWithRandomId();

        given(batchRepository.findExpiringSoon(CLINIC_ID, 30))
                .willReturn(List.of(expiringSoon1, expiringSoon2));

        // when
        var batches = batchService.getExpiringSoon(30);

        // then
        assertThat(batches).hasSize(2);
        verify(batchRepository).findExpiringSoon(CLINIC_ID, 30);
    }

    @Test
    void shouldCountPendingBatches() {
        // given
        given(batchRepository.countByClinicIdAndStatus(CLINIC_ID, BatchStatus.PENDING))
                .willReturn(5L);

        // when
        var count = batchService.countPendingBatches();

        // then
        assertThat(count).isEqualTo(5L);
        verify(batchRepository).countByClinicIdAndStatus(CLINIC_ID, BatchStatus.PENDING);
    }

    @Test
    void shouldGetBatchById() {
        // given
        var batchId = UUID.randomUUID();
        var batch = aCompleteBatch().withItemId(ITEM_ID).buildWithId(batchId);

        given(batchRepository.findById(batchId)).willReturn(Optional.of(batch));

        // when
        var result = batchService.getBatchById(batchId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(batchId);
        verify(batchRepository).findById(batchId);
    }

    // ==================== Error Scenarios (4 tests) ====================

    @Test
    void shouldThrowBatchNotFoundWhenGettingNonExistentBatch() {
        // given
        var unknownBatchId = UUID.randomUUID();
        given(batchRepository.findById(unknownBatchId)).willReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> batchService.getBatchById(unknownBatchId))
                .isInstanceOf(BatchNotFoundException.class)
                .hasMessageContaining(unknownBatchId.toString());
    }

    @Test
    void shouldThrowBatchNotFoundWhenCompletingNonExistentBatch() {
        // given
        var unknownBatchId = UUID.randomUUID();
        given(batchRepository.findById(unknownBatchId)).willReturn(Optional.empty());

        // when/then
        assertThatThrownBy(
                        () ->
                                batchService.completePendingBatch(
                                        unknownBatchId, "LOT-001", LocalDate.now().plusMonths(6)))
                .isInstanceOf(BatchNotFoundException.class)
                .hasMessageContaining(unknownBatchId.toString());
    }

    @Test
    void shouldThrowBatchNotFoundWhenDisposingNonExistentBatch() {
        // given
        var unknownBatchId = UUID.randomUUID();
        given(batchRepository.findById(unknownBatchId)).willReturn(Optional.empty());

        // when/then
        assertThatThrownBy(
                        () ->
                                batchService.disposeBatch(
                                        unknownBatchId, BigDecimal.valueOf(10), "Disposal reason"))
                .isInstanceOf(BatchNotFoundException.class)
                .hasMessageContaining(unknownBatchId.toString());
    }

    @Test
    void shouldThrowWhenConsumingFromEmptyBatches() {
        // given
        var item = createItem(ITEM_ID);
        given(batchRepository.findForFifoConsumption(ITEM_ID)).willReturn(Collections.emptyList());
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(item));

        // when/then
        assertThatThrownBy(
                        () ->
                                batchService.consumeStock(
                                        ITEM_ID, BigDecimal.valueOf(10), VISIT_ID, "VISIT"))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock")
                .hasMessageContaining("requested 10")
                .hasMessageContaining("available 0");
    }

    // ==================== Transaction Verification (3 tests) ====================

    @Test
    void shouldCreateTransactionWhenConsumingStock() {
        // given
        var batch =
                aBatch().withItemId(ITEM_ID)
                        .withQuantity(20)
                        .withLotNumber("LOT-TRX-001")
                        .withUnitCost(BigDecimal.valueOf(5.00))
                        .buildWithRandomId();

        given(batchRepository.findForFifoConsumption(ITEM_ID)).willReturn(List.of(batch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.valueOf(15));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        var transactionCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

        // when
        batchService.consumeStock(ITEM_ID, BigDecimal.valueOf(5), VISIT_ID, "VISIT");

        // then
        verify(transactionRepository).save(transactionCaptor.capture());
        var savedTransaction = transactionCaptor.getValue();

        assertThat(savedTransaction.getTransactionType()).isEqualTo(TransactionType.USAGE);
        assertThat(savedTransaction.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(-5));
        assertThat(savedTransaction.getQuantityBefore())
                .isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(savedTransaction.getQuantityAfter())
                .isEqualByComparingTo(BigDecimal.valueOf(15));
        assertThat(savedTransaction.getReferenceId()).isEqualTo(VISIT_ID);
        assertThat(savedTransaction.getReferenceType()).isEqualTo("VISIT");
        assertThat(savedTransaction.getBatchNumber()).isEqualTo("LOT-TRX-001");
    }

    @Test
    void shouldCreateExpiredTransactionWhenDisposing() {
        // given
        var batchId = UUID.randomUUID();
        var batch =
                aBatch().withItemId(ITEM_ID)
                        .withQuantity(30)
                        .withLotNumber("LOT-EXPIRED")
                        .withExpirationDate(LocalDate.now().minusDays(10))
                        .withUnitCost(BigDecimal.valueOf(8.00))
                        .buildWithId(batchId);

        given(batchRepository.findById(batchId)).willReturn(Optional.of(batch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.valueOf(20));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        var transactionCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

        // when
        batchService.disposeBatch(batchId, BigDecimal.valueOf(10), "Expired batch disposal");

        // then
        verify(transactionRepository).save(transactionCaptor.capture());
        var savedTransaction = transactionCaptor.getValue();

        assertThat(savedTransaction.getTransactionType()).isEqualTo(TransactionType.EXPIRED);
        assertThat(savedTransaction.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(-10));
        assertThat(savedTransaction.getQuantityBefore())
                .isEqualByComparingTo(BigDecimal.valueOf(30));
        assertThat(savedTransaction.getQuantityAfter())
                .isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(savedTransaction.getNotes()).isEqualTo("Expired batch disposal");
    }

    @Test
    void shouldRecalculateItemStockAfterConsumption() {
        // given
        var batch = aBatch().withItemId(ITEM_ID).withQuantity(50).buildWithRandomId();
        var item = createItem(ITEM_ID);
        item.setStockQuantity(BigDecimal.valueOf(50));

        given(batchRepository.findForFifoConsumption(ITEM_ID)).willReturn(List.of(batch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.valueOf(40));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(item));

        var itemCaptor = ArgumentCaptor.forClass(PriceListItem.class);

        // when
        batchService.consumeStock(ITEM_ID, BigDecimal.valueOf(10), VISIT_ID, "VISIT");

        // then
        verify(priceListRepository).save(itemCaptor.capture());
        var savedItem = itemCaptor.getValue();

        assertThat(savedItem.getStockQuantity()).isEqualByComparingTo(BigDecimal.valueOf(40));
    }

    // ==================== Edge Cases (2 tests) ====================

    @Test
    void shouldSkipDepletedBatchesInFIFOConsumption() {
        // given
        var depletedBatch =
                aDepletedBatch()
                        .withItemId(ITEM_ID)
                        .withLotNumber("LOT-DEPLETED")
                        .withExpirationDate(LocalDate.now().plusDays(5))
                        .buildWithRandomId();
        var activeBatch =
                aBatch().withItemId(ITEM_ID)
                        .withQuantity(20)
                        .withLotNumber("LOT-ACTIVE")
                        .withExpirationDate(LocalDate.now().plusDays(30))
                        .buildWithRandomId();

        // FIFO query returns only active batches (COMPLETE with quantity > 0)
        given(batchRepository.findForFifoConsumption(ITEM_ID)).willReturn(List.of(activeBatch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.valueOf(15));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        // when
        var consumed = batchService.consumeStock(ITEM_ID, BigDecimal.valueOf(5), VISIT_ID, "VISIT");

        // then
        assertThat(consumed).hasSize(1);
        assertThat(consumed.get(0).getLotNumber()).isEqualTo("LOT-ACTIVE");
        assertThat(depletedBatch.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO); // Unchanged
    }

    @Test
    void shouldConsumeExactBatchQuantity() {
        // given
        var batch =
                aBatch().withItemId(ITEM_ID)
                        .withQuantity(10)
                        .withLotNumber("LOT-EXACT")
                        .buildWithRandomId();

        given(batchRepository.findForFifoConsumption(ITEM_ID)).willReturn(List.of(batch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.ZERO);
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        // when - consume exactly what's in the batch
        var consumed =
                batchService.consumeStock(ITEM_ID, BigDecimal.valueOf(10), VISIT_ID, "VISIT");

        // then
        assertThat(consumed).hasSize(1);
        assertThat(consumed.get(0).getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(consumed.get(0).getStatus()).isEqualTo(BatchStatus.DEPLETED);
    }

    // ==================== Disposal Workflow Tests (4 tests) ====================

    @Test
    void shouldThrowWhenDisposingFromDepletedBatch() {
        // given
        var batchId = UUID.randomUUID();
        var batch = aDepletedBatch().withItemId(ITEM_ID).buildWithId(batchId);

        given(batchRepository.findById(batchId)).willReturn(Optional.of(batch));

        // when/then
        assertThatThrownBy(
                        () -> batchService.disposeBatch(batchId, BigDecimal.ONE, "Disposal reason"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot dispose 1");
    }

    @Test
    void shouldDisposeExactBatchQuantityAndDepleteBatch() {
        // given
        var batchId = UUID.randomUUID();
        var batch =
                aBatch().withItemId(ITEM_ID)
                        .withQuantity(25)
                        .withLotNumber("LOT-EXACT-DISPOSE")
                        .withUnitCost(BigDecimal.valueOf(5.00))
                        .buildWithId(batchId);

        given(batchRepository.findById(batchId)).willReturn(Optional.of(batch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.ZERO);
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        // when - dispose exactly 25 units
        var disposed =
                batchService.disposeBatch(batchId, BigDecimal.valueOf(25), "Full batch disposal");

        // then
        assertThat(disposed.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(disposed.getStatus()).isEqualTo(BatchStatus.DEPLETED);
    }

    @Test
    void shouldSetBatchIdOnDisposalTransaction() {
        // given
        var batchId = UUID.randomUUID();
        var batch =
                aBatch().withItemId(ITEM_ID)
                        .withQuantity(30)
                        .withLotNumber("LOT-TRX-DISPOSAL")
                        .withUnitCost(BigDecimal.valueOf(7.00))
                        .buildWithId(batchId);

        given(batchRepository.findById(batchId)).willReturn(Optional.of(batch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.valueOf(20));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        var transactionCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);

        // when
        batchService.disposeBatch(batchId, BigDecimal.valueOf(10), "Disposal for testing");

        // then
        verify(transactionRepository).save(transactionCaptor.capture());
        var savedTransaction = transactionCaptor.getValue();

        assertThat(savedTransaction.getBatchId()).isEqualTo(batchId);
        assertThat(savedTransaction.getReferenceType()).isEqualTo("DISPOSAL");
    }

    @Test
    void shouldAllowDisposingFromPendingBatch() {
        // given
        var batchId = UUID.randomUUID();
        var batch =
                aPendingBatch()
                        .withItemId(ITEM_ID)
                        .withQuantity(50)
                        .withUnitCost(BigDecimal.valueOf(6.00))
                        .buildWithId(batchId);

        given(batchRepository.findById(batchId)).willReturn(Optional.of(batch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.valueOf(30));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        // when - dispose 20 from pending batch with 50
        var disposed =
                batchService.disposeBatch(
                        batchId, BigDecimal.valueOf(20), "Partial disposal from pending");

        // then
        assertThat(disposed.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(30));
    }

    // ==================== Decimal/Fractional Quantity Tests (3 tests) ====================

    @Test
    void shouldConsumeFractionalQuantity() {
        // given - batch has 5.5 units
        var batch =
                aBatch().withItemId(ITEM_ID)
                        .withQuantity(BigDecimal.valueOf(5.5))
                        .withLotNumber("LOT-FRAC-001")
                        .buildWithRandomId();

        given(batchRepository.findForFifoConsumption(ITEM_ID)).willReturn(List.of(batch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.valueOf(4.75));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        // when - consume 0.75 units
        var consumed =
                batchService.consumeStock(ITEM_ID, BigDecimal.valueOf(0.75), VISIT_ID, "VISIT");

        // then - remaining should be 4.75
        assertThat(consumed).hasSize(1);
        assertThat(consumed.get(0).getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(4.75));
    }

    @Test
    void shouldConsumeFractionalQuantityAcrossMultipleBatches() {
        // given - batch1: 2.5, batch2: 3.0
        var batch1 =
                aBatch().withItemId(ITEM_ID)
                        .withQuantity(BigDecimal.valueOf(2.5))
                        .withLotNumber("LOT-FRAC-001")
                        .withExpirationDate(LocalDate.now().plusDays(10)) // Expires first
                        .buildWithRandomId();
        var batch2 =
                aBatch().withItemId(ITEM_ID)
                        .withQuantity(BigDecimal.valueOf(3.0))
                        .withLotNumber("LOT-FRAC-002")
                        .withExpirationDate(LocalDate.now().plusDays(30))
                        .buildWithRandomId();

        given(batchRepository.findForFifoConsumption(ITEM_ID)).willReturn(List.of(batch1, batch2));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.valueOf(1.25));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        // when - consume 4.25 units
        var consumed =
                batchService.consumeStock(ITEM_ID, BigDecimal.valueOf(4.25), VISIT_ID, "VISIT");

        // then - batch1 depleted (2.5), batch2 has 1.25 remaining (3.0 - 1.75)
        assertThat(consumed).hasSize(2);
        assertThat(batch1.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO); // All 2.5 consumed
        assertThat(batch1.getStatus()).isEqualTo(BatchStatus.DEPLETED);
        assertThat(batch2.getQuantity())
                .isEqualByComparingTo(BigDecimal.valueOf(1.25)); // 3.0 - 1.75
    }

    @Test
    void shouldDisposeFractionalQuantity() {
        // given - batch has 10.5
        var batchId = UUID.randomUUID();
        var batch =
                aBatch().withItemId(ITEM_ID)
                        .withQuantity(BigDecimal.valueOf(10.5))
                        .withLotNumber("LOT-FRAC-DISPOSE")
                        .buildWithId(batchId);

        given(batchRepository.findById(batchId)).willReturn(Optional.of(batch));
        given(batchRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(batchRepository.sumQuantityByItemId(ITEM_ID)).willReturn(BigDecimal.valueOf(7.25));
        given(priceListRepository.findById(ITEM_ID)).willReturn(Optional.of(createItem(ITEM_ID)));

        // when - dispose 3.25 units
        var disposed =
                batchService.disposeBatch(batchId, BigDecimal.valueOf(3.25), "Fractional disposal");

        // then - remaining should be 7.25
        assertThat(disposed.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(7.25));
    }

    // Helper methods

    private InventoryBatch createBatch(
            UUID itemId, int quantity, String lotNumber, LocalDate expiration) {
        var batch =
                InventoryBatch.builder()
                        .itemId(itemId)
                        .quantity(BigDecimal.valueOf(quantity))
                        .lotNumber(lotNumber)
                        .expirationDate(expiration)
                        .status(BatchStatus.COMPLETE)
                        .unitCost(BigDecimal.valueOf(10.00))
                        .build();
        setId(batch, UUID.randomUUID());
        return batch;
    }

    private PriceListItem createItem(UUID itemId) {
        var item =
                PriceListItem.builder()
                        .name("Test Item")
                        .stockQuantity(BigDecimal.valueOf(30))
                        .costPrice(BigDecimal.valueOf(10.00))
                        .sellPrice(BigDecimal.valueOf(20.00))
                        .build();
        setId(item, itemId);
        return item;
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getSuperclass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID", e);
        }
    }
}
