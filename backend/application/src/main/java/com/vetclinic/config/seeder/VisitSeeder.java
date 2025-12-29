package com.vetclinic.config.seeder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.vetclinic.billing.domain.model.ItemCategory;
import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.visit.domain.model.UsedMaterial;
import com.vetclinic.visit.domain.model.Visit;
import com.vetclinic.visit.domain.model.VisitStatus;
import com.vetclinic.visit.domain.model.VisitType;

import lombok.extern.slf4j.Slf4j;

/** Seeds visit entities including past, current, and future appointments. */
@Component
@Slf4j
public class VisitSeeder implements DataSeeder {

    private static final int WORK_DAY_START = 8;
    private static final int WORK_DAY_END = 17;
    private static final int PAST_DAYS = 60;
    private static final int FUTURE_DAYS = 14;
    private static final int[] DURATIONS = {15, 30, 30, 30, 45, 60};

    private static final String[] VISIT_REASONS = {
        "Annual checkup", "Vaccination", "Skin problem", "Limping", "Vomiting",
        "Diarrhea", "Ear infection", "Eye discharge", "Dental issue", "Weight management",
        "Allergy symptoms", "Follow-up examination", "Coughing", "Lethargy", "Not eating",
        "Wound care", "Post-surgery checkup", "Senior wellness exam"
    };

    @Override
    public int getOrder() {
        return 6;
    }

    @Override
    public void seed(EntityManager entityManager, SeedContext context) {
        var today = LocalDate.now();
        seedPastVisits(entityManager, context, today);
        seedTodayVisits(entityManager, context, today);
        seedFutureVisits(entityManager, context, today);
        seedCancelledVisits(entityManager, context, today);
        log.info("Created visits");
    }

    private void seedPastVisits(EntityManager entityManager, SeedContext context, LocalDate today) {
        for (int dayOffset = PAST_DAYS; dayOffset >= 1; dayOffset--) {
            var visitDate = today.minusDays(dayOffset);
            if (isWeekday(visitDate)) {
                var visitsPerDay = 2 + context.getRandom().nextInt(4);
                for (int i = 0; i < visitsPerDay; i++) {
                    var hour = WORK_DAY_START + context.getRandom().nextInt(9);
                    createVisit(entityManager, context, visitDate, hour, VisitStatus.COMPLETED);
                }
            }
        }
    }

    private void seedTodayVisits(
            EntityManager entityManager, SeedContext context, LocalDate today) {
        // Always create some completed visits for today (for stats demo)
        for (int i = 0; i < 3 + context.getRandom().nextInt(3); i++) {
            createVisit(entityManager, context, today, WORK_DAY_START + i, VisitStatus.COMPLETED);
        }

        if (isWeekday(today)) {
            var currentHour = LocalTime.now().getHour();
            if (currentHour >= WORK_DAY_START && currentHour <= WORK_DAY_END) {
                createVisit(entityManager, context, today, currentHour, VisitStatus.IN_PROGRESS);
            }

            // Create some CHECKED_IN visits for waiting room demo
            seedCheckedInVisits(entityManager, context, today);

            for (int hour = Math.max(currentHour + 1, 13); hour <= WORK_DAY_END; hour++) {
                if (context.getRandom().nextDouble() > 0.4) {
                    createVisit(entityManager, context, today, hour, VisitStatus.SCHEDULED);
                }
            }
        }
    }

    private void seedCheckedInVisits(
            EntityManager entityManager, SeedContext context, LocalDate today) {
        // Create 2-4 CHECKED_IN visits with different priorities for waiting room demo
        var priorities =
                new com.vetclinic.visit.domain.model.VisitPriority[] {
                    com.vetclinic.visit.domain.model.VisitPriority.NORMAL,
                    com.vetclinic.visit.domain.model.VisitPriority.HIGH,
                    com.vetclinic.visit.domain.model.VisitPriority.URGENT,
                    com.vetclinic.visit.domain.model.VisitPriority.LOW
                };
        var waitingRoomNotes =
                new String[] {
                    "Patient seems anxious",
                    "Brought stool sample",
                    "Owner reports urgent symptoms",
                    null
                };

        var currentHour = LocalTime.now().getHour();
        for (int i = 0; i < Math.min(4, priorities.length); i++) {
            var visit =
                    createVisit(
                            entityManager,
                            context,
                            today,
                            Math.max(WORK_DAY_START, currentHour - 1),
                            VisitStatus.CHECKED_IN);
            visit.setPriority(priorities[i]);
            visit.setWaitingRoomNotes(waitingRoomNotes[i]);
            // Set check-in time to simulate different wait times
            visit.setCheckedInAt(LocalDateTime.now().minusMinutes(5 + i * 15));
        }
    }

    private void seedFutureVisits(
            EntityManager entityManager, SeedContext context, LocalDate today) {
        for (int dayOffset = 1; dayOffset <= FUTURE_DAYS; dayOffset++) {
            var visitDate = today.plusDays(dayOffset);
            if (isWeekday(visitDate)) {
                var visitsPerDay = 1 + context.getRandom().nextInt(4);
                for (int i = 0; i < visitsPerDay; i++) {
                    var hour = WORK_DAY_START + context.getRandom().nextInt(10);
                    createVisit(entityManager, context, visitDate, hour, VisitStatus.SCHEDULED);
                }
            }
        }
    }

    private void seedCancelledVisits(
            EntityManager entityManager, SeedContext context, LocalDate today) {
        for (int i = 0; i < 5; i++) {
            var visitDate = today.minusDays(context.getRandom().nextInt(30));
            if (isWeekday(visitDate)) {
                var hour = WORK_DAY_START + context.getRandom().nextInt(10);
                createVisit(entityManager, context, visitDate, hour, VisitStatus.CANCELLED);
            }
        }
    }

    private Visit createVisit(
            EntityManager entityManager,
            SeedContext context,
            LocalDate date,
            int hour,
            VisitStatus status) {

        var random = context.getRandom();
        var patients = context.getPatients();
        var patient = patients.get(random.nextInt(patients.size()));
        var vetIndex = random.nextInt(context.getVeterinarianIds().size());
        var duration = DURATIONS[random.nextInt(DURATIONS.length)];
        var visitType = selectVisitType(random);
        var reason = selectReason(visitType, random);

        // Find client name for the patient's owner
        var clientName =
                context.getClients().stream()
                        .filter(c -> c.getId().equals(patient.getOwnerId()))
                        .findFirst()
                        .map(c -> c.getFirstName() + " " + c.getLastName())
                        .orElse(null);

        var visit =
                Visit.builder()
                        .patientId(patient.getId())
                        .patientName(patient.getName())
                        .clientId(patient.getOwnerId())
                        .clientName(clientName)
                        .veterinarianId(context.getVeterinarianIds().get(vetIndex))
                        .veterinarianName(context.getVeterinarianNames().get(vetIndex))
                        .visitDate(LocalDateTime.of(date, LocalTime.of(hour, 0)))
                        .durationMinutes(duration)
                        .status(status)
                        .visitType(visitType)
                        .reason(reason)
                        .build();

        if (status == VisitStatus.COMPLETED) {
            populateCompletedVisitDetails(visit, patient, reason, random);
            addUsedMaterials(visit, context);
        }

        visit.setClinicId(context.getClinicId());
        entityManager.persist(visit);

        if (status == VisitStatus.COMPLETED) {
            context.addCompletedVisit(visit);
        }

        return visit;
    }

    private void populateCompletedVisitDetails(
            Visit visit, Patient patient, String reason, Random random) {
        visit.setWeight(
                patient.getWeight() != null
                        ? Math.max(0.01, patient.getWeight() + (random.nextDouble() - 0.5))
                        : null);
        visit.setTemperature(38.0 + random.nextDouble() * 1.5);
        visit.setInterview(
                "Patient brought in for "
                        + reason.toLowerCase()
                        + ". Owner reports symptoms started "
                        + (1 + random.nextInt(7))
                        + " days ago.");
        visit.setExamination("Physical examination performed. Vital signs within normal limits.");
        visit.setDiagnosis(generateDiagnosis(reason));
        visit.setTreatment(generateTreatment(reason));
        visit.setRecommendations(
                "Continue current diet and exercise routine. Return in 2 weeks for follow-up.");
        visit.setNotes("Visit completed successfully.");
        if (random.nextDouble() > 0.7) {
            visit.setNextVisitDate(visit.getVisitDate().plusDays(7 + random.nextInt(21)));
        }
    }

    private void addUsedMaterials(Visit visit, SeedContext context) {
        var priceListItems = context.getPriceListItems();
        if (priceListItems.isEmpty()) return;

        var random = context.getRandom();
        var numMaterials = 1 + random.nextInt(4);

        var availableItems =
                priceListItems.stream()
                        .filter(
                                item -> {
                                    var isService =
                                            item.getCategory() == ItemCategory.CONSULTATION
                                                    || item.getCategory() == ItemCategory.PROCEDURE
                                                    || item.getCategory() == ItemCategory.LAB_TEST;
                                    return isService
                                            || (item.getStockQuantity() != null
                                                    && item.getStockQuantity() > 0);
                                })
                        .toList();

        if (availableItems.isEmpty()) return;

        for (int i = 0; i < numMaterials && i < availableItems.size(); i++) {
            var item = availableItems.get(random.nextInt(availableItems.size()));
            var isProduct =
                    item.getCategory() == ItemCategory.MEDICATION
                            || item.getCategory() == ItemCategory.PRODUCT;
            var quantity = isProduct ? 1 + random.nextInt(3) : 1;

            var usedMaterial =
                    UsedMaterial.builder()
                            .materialId(item.getId())
                            .name(item.getName())
                            .quantity(quantity)
                            .costPrice(item.getCostPrice())
                            .sellPrice(item.getSellPrice())
                            .unit(item.getUnit())
                            .build();
            visit.addUsedMaterial(usedMaterial);
        }
    }

    private VisitType selectVisitType(Random random) {
        VisitType[] types = {
            VisitType.CONSULTATION,
            VisitType.CONSULTATION,
            VisitType.CONSULTATION,
            VisitType.CHECKUP,
            VisitType.CHECKUP,
            VisitType.VACCINATION,
            VisitType.VACCINATION,
            VisitType.DEWORMING,
            VisitType.LAB_WORK,
            VisitType.FOLLOW_UP,
            VisitType.ULTRASOUND,
            VisitType.CARDIOLOGY,
            VisitType.DENTAL,
            VisitType.SURGERY,
            VisitType.GROOMING,
            VisitType.EMERGENCY
        };
        return types[random.nextInt(types.length)];
    }

    private String selectReason(VisitType type, Random random) {
        return switch (type) {
            case VACCINATION -> "Annual vaccination";
            case DEWORMING -> "Deworming treatment";
            case LAB_WORK -> "Blood work panel";
            case ULTRASOUND -> "Abdominal ultrasound";
            case CARDIOLOGY -> "Heart examination";
            case DENTAL -> "Dental cleaning";
            case SURGERY -> "Scheduled surgery";
            case GROOMING -> "Full grooming service";
            case EMERGENCY -> "Emergency consultation";
            case FOLLOW_UP -> "Follow-up examination";
            case CHECKUP -> "Annual checkup";
            default -> VISIT_REASONS[random.nextInt(VISIT_REASONS.length)];
        };
    }

    private String generateDiagnosis(String reason) {
        if (reason.contains("Vaccination")) return "Healthy patient, cleared for vaccination.";
        if (reason.contains("checkup"))
            return "Overall good health. Minor findings discussed with owner.";
        return "Condition assessed. Treatment plan established.";
    }

    private String generateTreatment(String reason) {
        if (reason.contains("Vaccination"))
            return "Administered scheduled vaccination. No adverse reactions.";
        return "Appropriate treatment administered.";
    }

    private boolean isWeekday(LocalDate date) {
        return date.getDayOfWeek().getValue() <= 5;
    }
}
