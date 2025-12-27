package com.vetclinic.config.seeder;

import java.math.BigDecimal;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.vetclinic.billing.domain.model.ItemCategory;
import com.vetclinic.billing.domain.model.PriceListItem;

import lombok.extern.slf4j.Slf4j;

/** Seeds price list items (services, medications, products). */
@Component
@Slf4j
public class PriceListSeeder implements DataSeeder {

    private static final int MIN_REORDER_POINT = 10;
    private static final int REORDER_POINT_RANGE = 15;
    private static final int STOCK_BUFFER = 10;
    private static final int STOCK_RANGE = 100;

    private static final double OUT_OF_STOCK_PROBABILITY = 0.05;
    private static final double LOW_STOCK_PROBABILITY = 0.15;

    @Override
    public int getOrder() {
        return 5;
    }

    @Override
    public void seed(EntityManager entityManager, SeedContext context) {
        seedConsultations(entityManager, context);
        seedVaccinations(entityManager, context);
        seedProcedures(entityManager, context);
        seedLabTests(entityManager, context);
        seedMedications(entityManager, context);
        seedProducts(entityManager, context);
        log.info("Created {} price list items", context.getPriceListItems().size());
    }

    private void seedConsultations(EntityManager entityManager, SeedContext context) {
        createItem(
                entityManager,
                context,
                "General Consultation",
                "Standard veterinary examination",
                ItemCategory.CONSULTATION,
                80,
                150,
                "visit",
                "CONS-001");
        createItem(
                entityManager,
                context,
                "Specialist Consultation",
                "Consultation with specialist veterinarian",
                ItemCategory.CONSULTATION,
                120,
                250,
                "visit",
                "CONS-002");
        createItem(
                entityManager,
                context,
                "Emergency Consultation",
                "After-hours emergency examination",
                ItemCategory.CONSULTATION,
                150,
                350,
                "visit",
                "CONS-003");
        createItem(
                entityManager,
                context,
                "Follow-up Consultation",
                "Follow-up examination",
                ItemCategory.CONSULTATION,
                50,
                100,
                "visit",
                "CONS-004");
    }

    private void seedVaccinations(EntityManager entityManager, SeedContext context) {
        createItem(
                entityManager,
                context,
                "Dog Vaccination - DHPP",
                "Distemper, Hepatitis, Parainfluenza, Parvovirus",
                ItemCategory.VACCINATION,
                40,
                120,
                "dose",
                "VAC-001");
        createItem(
                entityManager,
                context,
                "Dog Vaccination - Rabies",
                "Rabies vaccine for dogs",
                ItemCategory.VACCINATION,
                35,
                100,
                "dose",
                "VAC-002");
        createItem(
                entityManager,
                context,
                "Cat Vaccination - FVRCP",
                "Feline viral rhinotracheitis, calicivirus, panleukopenia",
                ItemCategory.VACCINATION,
                38,
                110,
                "dose",
                "VAC-003");
        createItem(
                entityManager,
                context,
                "Cat Vaccination - Rabies",
                "Rabies vaccine for cats",
                ItemCategory.VACCINATION,
                35,
                100,
                "dose",
                "VAC-004");
        createItem(
                entityManager,
                context,
                "Kennel Cough Vaccine",
                "Bordetella bronchiseptica vaccination",
                ItemCategory.VACCINATION,
                30,
                80,
                "dose",
                "VAC-005");
    }

    private void seedProcedures(EntityManager entityManager, SeedContext context) {
        createItem(
                entityManager,
                context,
                "Neutering - Male Dog",
                "Castration surgery for male dogs",
                ItemCategory.PROCEDURE,
                200,
                450,
                "surgery",
                "PROC-001");
        createItem(
                entityManager,
                context,
                "Spaying - Female Dog",
                "Ovariohysterectomy for female dogs",
                ItemCategory.PROCEDURE,
                300,
                650,
                "surgery",
                "PROC-002");
        createItem(
                entityManager,
                context,
                "Neutering - Male Cat",
                "Castration surgery for male cats",
                ItemCategory.PROCEDURE,
                100,
                250,
                "surgery",
                "PROC-003");
        createItem(
                entityManager,
                context,
                "Spaying - Female Cat",
                "Ovariohysterectomy for female cats",
                ItemCategory.PROCEDURE,
                150,
                350,
                "surgery",
                "PROC-004");
        createItem(
                entityManager,
                context,
                "Dental Cleaning",
                "Professional teeth cleaning and polishing",
                ItemCategory.PROCEDURE,
                150,
                400,
                "procedure",
                "PROC-005");
        createItem(
                entityManager,
                context,
                "Tooth Extraction",
                "Single tooth extraction",
                ItemCategory.PROCEDURE,
                50,
                150,
                "tooth",
                "PROC-006");
        createItem(
                entityManager,
                context,
                "Microchip Implantation",
                "Pet microchip insertion",
                ItemCategory.PROCEDURE,
                30,
                100,
                "chip",
                "PROC-007");
        createItem(
                entityManager,
                context,
                "Nail Trimming",
                "Professional nail clipping",
                ItemCategory.PROCEDURE,
                10,
                40,
                "procedure",
                "PROC-008");
        createItem(
                entityManager,
                context,
                "Ear Cleaning",
                "Professional ear cleaning",
                ItemCategory.PROCEDURE,
                15,
                50,
                "procedure",
                "PROC-009");
    }

    private void seedLabTests(EntityManager entityManager, SeedContext context) {
        createItem(
                entityManager,
                context,
                "Blood Test - Complete",
                "Complete blood count and chemistry panel",
                ItemCategory.LAB_TEST,
                80,
                200,
                "test",
                "LAB-001");
        createItem(
                entityManager,
                context,
                "Blood Test - Basic",
                "Basic blood chemistry",
                ItemCategory.LAB_TEST,
                40,
                100,
                "test",
                "LAB-002");
        createItem(
                entityManager,
                context,
                "Urinalysis",
                "Complete urine analysis",
                ItemCategory.LAB_TEST,
                30,
                80,
                "test",
                "LAB-003");
        createItem(
                entityManager,
                context,
                "Fecal Examination",
                "Parasite screening",
                ItemCategory.LAB_TEST,
                25,
                60,
                "test",
                "LAB-004");
        createItem(
                entityManager,
                context,
                "X-Ray - Single View",
                "Single radiograph",
                ItemCategory.LAB_TEST,
                60,
                150,
                "image",
                "LAB-005");
        createItem(
                entityManager,
                context,
                "X-Ray - Multiple Views",
                "Multiple radiograph views",
                ItemCategory.LAB_TEST,
                100,
                250,
                "series",
                "LAB-006");
        createItem(
                entityManager,
                context,
                "Ultrasound",
                "Diagnostic ultrasound examination",
                ItemCategory.LAB_TEST,
                120,
                300,
                "exam",
                "LAB-007");
    }

    private void seedMedications(EntityManager entityManager, SeedContext context) {
        createItem(
                entityManager,
                context,
                "Antibiotic - Amoxicillin",
                "Broad spectrum antibiotic",
                ItemCategory.MEDICATION,
                15,
                45,
                "course",
                "MED-001");
        createItem(
                entityManager,
                context,
                "Anti-inflammatory - Meloxicam",
                "Non-steroidal anti-inflammatory",
                ItemCategory.MEDICATION,
                20,
                55,
                "course",
                "MED-002");
        createItem(
                entityManager,
                context,
                "Flea Treatment - Frontline",
                "Monthly flea and tick prevention",
                ItemCategory.MEDICATION,
                25,
                60,
                "dose",
                "MED-003");
        createItem(
                entityManager,
                context,
                "Dewormer - Drontal",
                "Broad spectrum dewormer",
                ItemCategory.MEDICATION,
                15,
                40,
                "tablet",
                "MED-004");
        createItem(
                entityManager,
                context,
                "Eye Drops - Antibiotic",
                "Ophthalmic antibiotic solution",
                ItemCategory.MEDICATION,
                18,
                50,
                "bottle",
                "MED-005");
        createItem(
                entityManager,
                context,
                "Ear Drops - Otitis Treatment",
                "Ear infection treatment",
                ItemCategory.MEDICATION,
                22,
                60,
                "bottle",
                "MED-006");
        createItem(
                entityManager,
                context,
                "Pain Relief - Tramadol",
                "Prescription pain medication",
                ItemCategory.MEDICATION,
                25,
                70,
                "course",
                "MED-007");
    }

    private void seedProducts(EntityManager entityManager, SeedContext context) {
        createItem(
                entityManager,
                context,
                "Elizabethan Collar - Small",
                "Recovery cone small size",
                ItemCategory.PRODUCT,
                8,
                25,
                "piece",
                "PROD-001");
        createItem(
                entityManager,
                context,
                "Elizabethan Collar - Medium",
                "Recovery cone medium size",
                ItemCategory.PRODUCT,
                10,
                30,
                "piece",
                "PROD-002");
        createItem(
                entityManager,
                context,
                "Elizabethan Collar - Large",
                "Recovery cone large size",
                ItemCategory.PRODUCT,
                12,
                35,
                "piece",
                "PROD-003");
        createItem(
                entityManager,
                context,
                "Bandage - Elastic",
                "Self-adhesive elastic bandage",
                ItemCategory.PRODUCT,
                5,
                15,
                "roll",
                "PROD-004");
        createItem(
                entityManager,
                context,
                "Surgical Sutures",
                "Absorbable surgical sutures",
                ItemCategory.PRODUCT,
                20,
                50,
                "pack",
                "PROD-005");
    }

    private void createItem(
            EntityManager entityManager,
            SeedContext context,
            String name,
            String description,
            ItemCategory category,
            double cost,
            double sell,
            String unit,
            String code) {

        var isService =
                category == ItemCategory.CONSULTATION
                        || category == ItemCategory.PROCEDURE
                        || category == ItemCategory.LAB_TEST;

        Integer stockQty = null;
        Integer reorderPt = null;

        if (!isService) {
            reorderPt = MIN_REORDER_POINT + context.getRandom().nextInt(REORDER_POINT_RANGE);
            stockQty = calculateStockQuantity(reorderPt, context);
        }

        var item =
                PriceListItem.builder()
                        .name(name)
                        .description(description)
                        .category(category)
                        .costPrice(BigDecimal.valueOf(cost))
                        .sellPrice(BigDecimal.valueOf(sell))
                        .unit(unit)
                        .code(code)
                        .stockQuantity(stockQty)
                        .reorderPoint(reorderPt)
                        .active(true)
                        .build();
        item.setClinicId(context.getClinicId());
        entityManager.persist(item);
        context.addPriceListItem(item);
    }

    private Integer calculateStockQuantity(int reorderPoint, SeedContext context) {
        var random = context.getRandom();
        var stockLevel = random.nextDouble();

        if (stockLevel < OUT_OF_STOCK_PROBABILITY) {
            return 0; // Out of stock
        } else if (stockLevel < LOW_STOCK_PROBABILITY) {
            return 1 + random.nextInt(reorderPoint); // Low stock
        } else {
            return reorderPoint + STOCK_BUFFER + random.nextInt(STOCK_RANGE); // In stock
        }
    }
}
