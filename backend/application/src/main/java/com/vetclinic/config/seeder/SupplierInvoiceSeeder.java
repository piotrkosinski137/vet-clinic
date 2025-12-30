package com.vetclinic.config.seeder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.vetclinic.billing.domain.model.InventoryTransaction;
import com.vetclinic.billing.domain.model.ItemCategory;
import com.vetclinic.billing.domain.model.PriceListItem;
import com.vetclinic.billing.domain.model.SupplierInvoice;
import com.vetclinic.billing.domain.model.SupplierInvoiceItem;
import com.vetclinic.billing.domain.model.SupplierInvoiceStatus;
import com.vetclinic.billing.domain.model.TransactionType;

import lombok.extern.slf4j.Slf4j;

/** Seeds supplier invoices for inventory management demo. Runs after PriceListSeeder. */
@Component
@Slf4j
public class SupplierInvoiceSeeder implements DataSeeder {

    private static final String[] SUPPLIERS = {
        "Eurovet Sp. z o.o.",
        "VetAgri Polska",
        "MediVet Dystrybucja",
        "Fatro Polska",
        "Biowet Puławy"
    };

    private static final String[] SUPPLIER_NIPS = {
        "5261234567", "7891234567", "1234567890", "9876543210", "5432167890"
    };

    @Override
    public int getOrder() {
        return 8; // After InvoiceSeeder (7)
    }

    @Override
    public void seed(EntityManager entityManager, SeedContext context) {
        var random = context.getRandom();
        var items = context.getPriceListItems();
        var stockableItems = new ArrayList<PriceListItem>();

        // Get stockable items (medications, vaccinations, supplies)
        for (var item : items) {
            if (!isService(item.getCategory())) {
                stockableItems.add(item);
            }
        }

        if (stockableItems.isEmpty()) {
            log.warn("No stockable items found for supplier invoice seeding");
            return;
        }

        var invoiceCount = 0;
        var transactionCount = 0;

        // Create 5 supplier invoices over the last 60 days
        for (var i = 0; i < 5; i++) {
            var supplierIdx = i % SUPPLIERS.length;
            var daysAgo = random.nextInt(60);
            var invoiceDate = LocalDate.now().minusDays(daysAgo);
            var invoiceNumber = String.format("FV/%d/%04d", invoiceDate.getYear(), 1000 + i);

            // Determine status - older invoices are more likely to be processed
            var status =
                    daysAgo > 14
                            ? SupplierInvoiceStatus.PROCESSED
                            : (daysAgo > 7
                                    ? (random.nextBoolean()
                                            ? SupplierInvoiceStatus.PROCESSED
                                            : SupplierInvoiceStatus.PENDING)
                                    : SupplierInvoiceStatus.PENDING);

            var invoice =
                    SupplierInvoice.builder()
                            .invoiceNumber(invoiceNumber)
                            .supplierName(SUPPLIERS[supplierIdx])
                            .supplierNip(SUPPLIER_NIPS[supplierIdx])
                            .invoiceDate(invoiceDate)
                            .saleDate(invoiceDate)
                            .paymentDueDate(invoiceDate.plusDays(30))
                            .paymentMethod("Przelew 30 dni")
                            .status(status)
                            .processedAt(
                                    status == SupplierInvoiceStatus.PROCESSED
                                            ? LocalDateTime.now().minusDays(daysAgo - 1)
                                            : null)
                            .build();
            invoice.setClinicId(context.getClinicId());

            // Add 3-6 items per invoice
            var itemCount = 3 + random.nextInt(4);
            var totalNet = BigDecimal.ZERO;
            var totalVat = BigDecimal.ZERO;

            for (var j = 0; j < itemCount && j < stockableItems.size(); j++) {
                var priceItem = stockableItems.get((i * 3 + j) % stockableItems.size());
                var qty = 5 + random.nextInt(20);
                var netPrice =
                        priceItem.getCostPrice() != null
                                ? priceItem.getCostPrice()
                                : BigDecimal.valueOf(10 + random.nextInt(90));
                var vatRate = 23;
                var vatAmount =
                        netPrice.multiply(BigDecimal.valueOf(qty))
                                .multiply(BigDecimal.valueOf(vatRate))
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                var grossPrice =
                        netPrice.multiply(BigDecimal.valueOf(100 + vatRate))
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                var invoiceItem =
                        SupplierInvoiceItem.builder()
                                .productCode(priceItem.getCode())
                                .productName(priceItem.getName())
                                .quantity(qty)
                                .unit("szt.")
                                .netPrice(netPrice)
                                .grossPrice(grossPrice)
                                .vatRate(vatRate)
                                .vatAmount(vatAmount)
                                .batchNumber("LOT-" + invoiceNumber.replace("/", "-") + "-" + j)
                                .expirationDate(LocalDate.now().plusMonths(12 + random.nextInt(24)))
                                .matched(status == SupplierInvoiceStatus.PROCESSED)
                                .itemId(
                                        status == SupplierInvoiceStatus.PROCESSED
                                                ? priceItem.getId()
                                                : null)
                                .build();

                invoice.addItem(invoiceItem);
                totalNet = totalNet.add(netPrice.multiply(BigDecimal.valueOf(qty)));
                totalVat = totalVat.add(vatAmount);

                // Create RECEIPT transaction for processed invoices
                if (status == SupplierInvoiceStatus.PROCESSED) {
                    var currentStock =
                            priceItem.getStockQuantity() != null
                                    ? priceItem.getStockQuantity()
                                    : BigDecimal.ZERO;
                    var transaction =
                            InventoryTransaction.builder()
                                    .itemId(priceItem.getId())
                                    .transactionType(TransactionType.RECEIPT)
                                    .quantity(BigDecimal.valueOf(qty))
                                    .quantityBefore(currentStock.subtract(BigDecimal.valueOf(qty)))
                                    .quantityAfter(currentStock)
                                    .referenceType("SUPPLIER_INVOICE")
                                    .batchNumber(invoiceItem.getBatchNumber())
                                    .expirationDate(invoiceItem.getExpirationDate())
                                    .unitCost(netPrice)
                                    .notes("From invoice: " + invoiceNumber)
                                    .build();
                    transaction.setClinicId(context.getClinicId());
                    entityManager.persist(transaction);
                    transactionCount++;
                }
            }

            invoice.setTotalNet(totalNet);
            invoice.setTotalVat(totalVat);
            invoice.setTotalGross(totalNet.add(totalVat));

            entityManager.persist(invoice);
            invoiceCount++;
        }

        log.info(
                "Created {} supplier invoices with {} RECEIPT transactions",
                invoiceCount,
                transactionCount);
    }

    private boolean isService(ItemCategory category) {
        return category == ItemCategory.CONSULTATION
                || category == ItemCategory.PROCEDURE
                || category == ItemCategory.LAB_TEST;
    }
}
