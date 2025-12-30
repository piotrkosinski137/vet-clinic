package com.vetclinic.config.seeder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.vetclinic.billing.domain.model.BatchStatus;
import com.vetclinic.billing.domain.model.InventoryBatch;
import com.vetclinic.billing.domain.model.InventoryTransaction;
import com.vetclinic.billing.domain.model.ItemCategory;
import com.vetclinic.billing.domain.model.PriceListItem;
import com.vetclinic.billing.domain.model.TransactionType;

import lombok.extern.slf4j.Slf4j;

/** Seeds inventory batches for items with stock (FIFO tracking). Runs after PriceListSeeder. */
@Component
@Slf4j
public class InventoryBatchSeeder implements DataSeeder {

    private static final double COMPLETE_PROBABILITY = 0.80;
    private static final double PENDING_PROBABILITY = 0.15;
    // DEPLETED = remaining 0.05

    private static final double EXPIRING_SOON_PROBABILITY = 0.20;
    private static final double EXPIRED_PROBABILITY = 0.10;

    private static final int MAX_BATCHES_PER_ITEM = 3;
    private static final int MIN_EXPIRATION_DAYS = 90;
    private static final int MAX_EXPIRATION_DAYS = 730;
    private static final int EXPIRING_SOON_DAYS = 30;

    private final AtomicInteger batchCounter = new AtomicInteger(0);

    @Override
    public int getOrder() {
        return 6; // After PriceListSeeder (5)
    }

    @Override
    public void seed(EntityManager entityManager, SeedContext context) {
        var items = context.getPriceListItems();
        var batchCount = 0;

        for (var item : items) {
            // Skip services (no stock tracking)
            if (isService(item.getCategory())) {
                continue;
            }

            // Only create batches for items with stock
            if (item.getStockQuantity() == null
                    || item.getStockQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            batchCount += createBatchesForItem(entityManager, context, item);
        }

        log.info("Created {} inventory batches for {} stockable items", batchCount, items.size());

        // Add explicit test scenarios
        createFifoTestScenarios(entityManager, context);
        createPendingBatchScenarios(entityManager, context);
        createExpiredBatchScenarios(entityManager, context);
        createDisposalHistoryScenarios(entityManager, context);
        createUsageTransactions(entityManager, context);

        log.info(
                "Created inventory batches with FIFO, test scenarios, disposal history, and usage transactions");
    }

    private boolean isService(ItemCategory category) {
        return category == ItemCategory.CONSULTATION
                || category == ItemCategory.PROCEDURE
                || category == ItemCategory.LAB_TEST;
    }

    private int createBatchesForItem(
            EntityManager entityManager, SeedContext context, PriceListItem item) {
        var random = context.getRandom();
        var totalStock = item.getStockQuantity().intValue();
        var numBatches = 1 + random.nextInt(MAX_BATCHES_PER_ITEM); // 1-3 batches
        var remainingStock = totalStock;
        var created = 0;

        for (var i = 0; i < numBatches && remainingStock > 0; i++) {
            var isLastBatch = i == numBatches - 1;
            var batchQty =
                    isLastBatch
                            ? remainingStock
                            : distributeQuantity(remainingStock, numBatches - i, random);

            var batch = createBatch(item, batchQty, context);
            batch.setClinicId(context.getClinicId());
            entityManager.persist(batch);

            remainingStock -= batchQty;
            created++;
        }

        return created;
    }

    private int distributeQuantity(int remaining, int batchesLeft, java.util.Random random) {
        if (batchesLeft <= 1) {
            return remaining;
        }
        // Distribute roughly evenly with some variance
        var avg = remaining / batchesLeft;
        var variance = Math.max(1, avg / 2);
        return Math.max(1, avg + random.nextInt(variance) - variance / 2);
    }

    private InventoryBatch createBatch(PriceListItem item, int quantity, SeedContext context) {
        var random = context.getRandom();
        var statusRoll = random.nextDouble();

        BatchStatus status;
        if (statusRoll < COMPLETE_PROBABILITY) {
            status = BatchStatus.COMPLETE;
        } else if (statusRoll < COMPLETE_PROBABILITY + PENDING_PROBABILITY) {
            status = BatchStatus.PENDING;
        } else {
            status = BatchStatus.DEPLETED;
        }

        // DEPLETED batches have 0 quantity
        var batchQty = BigDecimal.valueOf(status == BatchStatus.DEPLETED ? 0 : quantity);

        // Generate LOT number for COMPLETE batches
        String lotNumber = null;
        if (status == BatchStatus.COMPLETE) {
            lotNumber = generateLotNumber(item.getCode());
        }

        // Generate expiration date for COMPLETE batches
        LocalDate expirationDate = null;
        if (status == BatchStatus.COMPLETE) {
            expirationDate = generateExpirationDate(random);
        }

        return InventoryBatch.builder()
                .itemId(item.getId())
                .lotNumber(lotNumber)
                .expirationDate(expirationDate)
                .quantity(batchQty)
                .unitCost(item.getCostPrice())
                .status(status)
                .build();
    }

    private String generateLotNumber(String itemCode) {
        var counter = batchCounter.incrementAndGet();
        var prefix =
                itemCode != null ? itemCode.substring(0, Math.min(3, itemCode.length())) : "LOT";
        return String.format("%s-%04d-%02d", prefix, 2024, counter);
    }

    private LocalDate generateExpirationDate(java.util.Random random) {
        var expirationRoll = random.nextDouble();

        if (expirationRoll < EXPIRED_PROBABILITY) {
            // Already expired (1-30 days ago)
            return LocalDate.now().minusDays(1 + random.nextInt(30));
        } else if (expirationRoll < EXPIRED_PROBABILITY + EXPIRING_SOON_PROBABILITY) {
            // Expiring soon (1-30 days)
            return LocalDate.now().plusDays(1 + random.nextInt(EXPIRING_SOON_DAYS));
        } else {
            // Normal expiration (90-730 days)
            return LocalDate.now()
                    .plusDays(
                            MIN_EXPIRATION_DAYS
                                    + random.nextInt(MAX_EXPIRATION_DAYS - MIN_EXPIRATION_DAYS));
        }
    }

    /**
     * Creates explicit FIFO test scenarios for the first 3 stockable items with sufficient stock
     * (>50 units). Each item gets 3 batches with staggered expiration dates to demonstrate FIFO
     * consumption order.
     */
    private void createFifoTestScenarios(EntityManager entityManager, SeedContext context) {
        var items = context.getPriceListItems();
        var fifoItems = new ArrayList<PriceListItem>();

        // Find first 3 stockable items with >50 units
        for (var item : items) {
            if (isService(item.getCategory())) {
                continue;
            }
            if (item.getStockQuantity() != null
                    && item.getStockQuantity().compareTo(BigDecimal.valueOf(50)) > 0) {
                fifoItems.add(item);
                if (fifoItems.size() >= 3) {
                    break;
                }
            }
        }

        var fifoCount = 0;
        for (var item : fifoItems) {
            var itemCode = item.getCode() != null ? item.getCode() : "ITEM";

            // Batch 1: qty=20, exp=+15 days (should be consumed FIRST)
            var batch1 =
                    InventoryBatch.builder()
                            .itemId(item.getId())
                            .lotNumber("FIFO-" + itemCode + "-001")
                            .expirationDate(LocalDate.now().plusDays(15))
                            .quantity(BigDecimal.valueOf(20))
                            .unitCost(item.getCostPrice())
                            .status(BatchStatus.COMPLETE)
                            .build();
            batch1.setClinicId(context.getClinicId());
            entityManager.persist(batch1);

            // Batch 2: qty=30, exp=+45 days (should be consumed SECOND)
            var batch2 =
                    InventoryBatch.builder()
                            .itemId(item.getId())
                            .lotNumber("FIFO-" + itemCode + "-002")
                            .expirationDate(LocalDate.now().plusDays(45))
                            .quantity(BigDecimal.valueOf(30))
                            .unitCost(item.getCostPrice())
                            .status(BatchStatus.COMPLETE)
                            .build();
            batch2.setClinicId(context.getClinicId());
            entityManager.persist(batch2);

            // Batch 3: qty=50, exp=+90 days (should be consumed LAST)
            var batch3 =
                    InventoryBatch.builder()
                            .itemId(item.getId())
                            .lotNumber("FIFO-" + itemCode + "-003")
                            .expirationDate(LocalDate.now().plusDays(90))
                            .quantity(BigDecimal.valueOf(50))
                            .unitCost(item.getCostPrice())
                            .status(BatchStatus.COMPLETE)
                            .build();
            batch3.setClinicId(context.getClinicId());
            entityManager.persist(batch3);

            fifoCount += 3;
        }

        log.info("Created {} FIFO test batches for {} items", fifoCount, fifoItems.size());
    }

    /**
     * Creates 4-5 explicit PENDING batches for different items that need LOT assignment. These
     * demonstrate the pending batch workflow where LOT numbers and expiration dates must be
     * assigned before the batch can be used.
     */
    private void createPendingBatchScenarios(EntityManager entityManager, SeedContext context) {
        var items = context.getPriceListItems();
        var pendingItems = new ArrayList<PriceListItem>();

        // Find medications and vaccinations for pending batch scenarios
        for (var item : items) {
            var category = item.getCategory();
            if (category == ItemCategory.MEDICATION || category == ItemCategory.VACCINATION) {
                pendingItems.add(item);
                if (pendingItems.size() >= 5) {
                    break;
                }
            }
        }

        var pendingCount = 0;
        for (var item : pendingItems) {
            var batch =
                    InventoryBatch.builder()
                            .itemId(item.getId())
                            .lotNumber(null) // Needs LOT assignment
                            .expirationDate(null) // Needs expiration assignment
                            .quantity(BigDecimal.valueOf(100))
                            .unitCost(item.getCostPrice())
                            .status(BatchStatus.PENDING)
                            .build();
            batch.setClinicId(context.getClinicId());
            entityManager.persist(batch);
            pendingCount++;
        }

        log.info("Created {} pending batches for LOT assignment workflow", pendingCount);
    }

    /**
     * Creates 3 explicit expired batches for disposal testing. These batches have past expiration
     * dates and are ready for disposal workflow testing.
     */
    private void createExpiredBatchScenarios(EntityManager entityManager, SeedContext context) {
        var items = context.getPriceListItems();
        var expiredItems = new ArrayList<PriceListItem>();

        // Find stockable items for expired batch scenarios
        for (var item : items) {
            if (isService(item.getCategory())) {
                continue;
            }
            if (item.getStockQuantity() != null
                    && item.getStockQuantity().compareTo(BigDecimal.ZERO) > 0) {
                expiredItems.add(item);
                if (expiredItems.size() >= 3) {
                    break;
                }
            }
        }

        if (expiredItems.size() >= 1) {
            var item1 = expiredItems.get(0);
            var itemCode1 = item1.getCode() != null ? item1.getCode() : "ITEM";
            var batch1 =
                    InventoryBatch.builder()
                            .itemId(item1.getId())
                            .lotNumber("EXPIRED-" + itemCode1 + "-001")
                            .expirationDate(LocalDate.now().minusDays(15)) // 15 days ago
                            .quantity(BigDecimal.valueOf(25))
                            .unitCost(item1.getCostPrice())
                            .status(BatchStatus.COMPLETE)
                            .build();
            batch1.setClinicId(context.getClinicId());
            entityManager.persist(batch1);
        }

        if (expiredItems.size() >= 2) {
            var item2 = expiredItems.get(1);
            var itemCode2 = item2.getCode() != null ? item2.getCode() : "ITEM";
            var batch2 =
                    InventoryBatch.builder()
                            .itemId(item2.getId())
                            .lotNumber("EXPIRED-" + itemCode2 + "-002")
                            .expirationDate(LocalDate.now().minusDays(45)) // 45 days ago
                            .quantity(BigDecimal.valueOf(10))
                            .unitCost(item2.getCostPrice())
                            .status(BatchStatus.COMPLETE)
                            .build();
            batch2.setClinicId(context.getClinicId());
            entityManager.persist(batch2);
        }

        if (expiredItems.size() >= 3) {
            var item3 = expiredItems.get(2);
            var itemCode3 = item3.getCode() != null ? item3.getCode() : "ITEM";
            var batch3 =
                    InventoryBatch.builder()
                            .itemId(item3.getId())
                            .lotNumber("EXPIRED-" + itemCode3 + "-003")
                            .expirationDate(LocalDate.now().minusDays(5)) // 5 days ago
                            .quantity(BigDecimal.valueOf(40))
                            .unitCost(item3.getCostPrice())
                            .status(BatchStatus.COMPLETE)
                            .build();
            batch3.setClinicId(context.getClinicId());
            entityManager.persist(batch3);
        }

        log.info(
                "Created {} expired batches for disposal testing",
                Math.min(expiredItems.size(), 3));
    }

    /**
     * Creates disposal transactions and DEPLETED batches to demonstrate audit trail. This shows
     * what the system looks like after disposal workflow has been used.
     */
    private void createDisposalHistoryScenarios(EntityManager entityManager, SeedContext context) {
        var items = context.getPriceListItems();
        var disposalItems = new ArrayList<PriceListItem>();

        // Find 2 stockable items for disposal scenarios
        for (var item : items) {
            if (isService(item.getCategory())) {
                continue;
            }
            if (item.getStockQuantity() != null
                    && item.getStockQuantity().compareTo(BigDecimal.ZERO) > 0) {
                disposalItems.add(item);
                if (disposalItems.size() >= 2) {
                    break;
                }
            }
        }

        var count = 0;

        // Scenario 1: DEPLETED batch with full disposal transaction
        if (disposalItems.size() >= 1) {
            var item1 = disposalItems.get(0);
            var itemCode1 = item1.getCode() != null ? item1.getCode() : "ITEM";

            var depletedBatch =
                    InventoryBatch.builder()
                            .itemId(item1.getId())
                            .lotNumber("DISPOSED-" + itemCode1 + "-001")
                            .expirationDate(LocalDate.now().minusDays(30))
                            .quantity(BigDecimal.ZERO)
                            .unitCost(item1.getCostPrice())
                            .status(BatchStatus.DEPLETED)
                            .build();
            depletedBatch.setClinicId(context.getClinicId());
            entityManager.persist(depletedBatch);
            entityManager.flush();

            var transaction1 =
                    InventoryTransaction.builder()
                            .itemId(item1.getId())
                            .transactionType(TransactionType.EXPIRED)
                            .quantity(BigDecimal.valueOf(-15))
                            .quantityBefore(BigDecimal.valueOf(15))
                            .quantityAfter(BigDecimal.ZERO)
                            .referenceType("DISPOSAL")
                            .batchId(depletedBatch.getId())
                            .batchNumber(depletedBatch.getLotNumber())
                            .expirationDate(depletedBatch.getExpirationDate())
                            .unitCost(item1.getCostPrice())
                            .notes("Disposed expired batch - demonstration data")
                            .build();
            transaction1.setClinicId(context.getClinicId());
            entityManager.persist(transaction1);
            count++;
        }

        // Scenario 2: Partial disposal (5 remaining after disposing 10)
        if (disposalItems.size() >= 2) {
            var item2 = disposalItems.get(1);
            var itemCode2 = item2.getCode() != null ? item2.getCode() : "ITEM";

            var partialBatch =
                    InventoryBatch.builder()
                            .itemId(item2.getId())
                            .lotNumber("PARTIAL-DISPOSED-" + itemCode2)
                            .expirationDate(LocalDate.now().plusDays(60))
                            .quantity(BigDecimal.valueOf(5))
                            .unitCost(item2.getCostPrice())
                            .status(BatchStatus.COMPLETE)
                            .build();
            partialBatch.setClinicId(context.getClinicId());
            entityManager.persist(partialBatch);
            entityManager.flush();

            var transaction2 =
                    InventoryTransaction.builder()
                            .itemId(item2.getId())
                            .transactionType(TransactionType.EXPIRED)
                            .quantity(BigDecimal.valueOf(-10))
                            .quantityBefore(BigDecimal.valueOf(15))
                            .quantityAfter(BigDecimal.valueOf(5))
                            .referenceType("DISPOSAL")
                            .batchId(partialBatch.getId())
                            .batchNumber(partialBatch.getLotNumber())
                            .expirationDate(partialBatch.getExpirationDate())
                            .unitCost(item2.getCostPrice())
                            .notes("Partial disposal - damaged units removed")
                            .build();
            transaction2.setClinicId(context.getClinicId());
            entityManager.persist(transaction2);
            count++;
        }

        log.info("Created {} disposal history scenarios with transactions", count);
    }

    /**
     * Creates USAGE transactions to simulate items being used in visits. Shows realistic inventory
     * consumption patterns.
     */
    private void createUsageTransactions(EntityManager entityManager, SeedContext context) {
        var items = context.getPriceListItems();
        var random = context.getRandom();
        var usageCount = 0;

        // Create usage transactions for stockable items
        for (var item : items) {
            if (isService(item.getCategory())) {
                continue;
            }
            if (item.getStockQuantity() == null
                    || item.getStockQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // Create 2-5 usage transactions per item
            var numUsages = 2 + random.nextInt(4);
            var currentStock = item.getStockQuantity().intValue();

            for (var i = 0; i < numUsages && currentStock > 5; i++) {
                var usageQty = 1 + random.nextInt(3);
                var daysAgo = random.nextInt(30);

                var transaction =
                        InventoryTransaction.builder()
                                .itemId(item.getId())
                                .transactionType(TransactionType.USAGE)
                                .quantity(BigDecimal.valueOf(-usageQty))
                                .quantityBefore(BigDecimal.valueOf(currentStock))
                                .quantityAfter(BigDecimal.valueOf(currentStock - usageQty))
                                .referenceType("VISIT")
                                .referenceId(java.util.UUID.randomUUID())
                                .batchNumber(
                                        item.getCode() != null
                                                ? "LOT-" + item.getCode() + "-001"
                                                : null)
                                .unitCost(item.getCostPrice())
                                .notes("Used during patient visit")
                                .build();
                transaction.setClinicId(context.getClinicId());
                entityManager.persist(transaction);

                currentStock -= usageQty;
                usageCount++;
            }
        }

        log.info("Created {} USAGE transactions", usageCount);
    }
}
