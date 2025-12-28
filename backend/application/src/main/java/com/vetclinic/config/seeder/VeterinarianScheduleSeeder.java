package com.vetclinic.config.seeder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.vetclinic.veterinarian.domain.model.DayOffType;
import com.vetclinic.veterinarian.domain.model.VeterinarianDayOff;
import com.vetclinic.veterinarian.domain.model.VeterinarianSchedule;

import lombok.extern.slf4j.Slf4j;

/** Seeds veterinarian schedules and days off. */
@Component
@Slf4j
public class VeterinarianScheduleSeeder implements DataSeeder {

    private static final LocalTime MORNING_START = LocalTime.of(8, 0);
    private static final LocalTime AFTERNOON_END = LocalTime.of(17, 0);
    private static final LocalTime EARLY_END = LocalTime.of(14, 0);
    private static final LocalTime LATE_START = LocalTime.of(10, 0);
    private static final LocalTime SATURDAY_START = LocalTime.of(9, 0);
    private static final LocalTime SATURDAY_END = LocalTime.of(13, 0);

    @Override
    public int getOrder() {
        // Run after veterinarians are created (order 2) but before visits (order 6)
        return 3;
    }

    @Override
    public void seed(EntityManager entityManager, SeedContext context) {
        var vetIds = context.getVeterinarianIds();

        for (int i = 0; i < vetIds.size(); i++) {
            var vetId = vetIds.get(i);
            createScheduleForVet(entityManager, context, vetId, i);
            createDaysOffForVet(entityManager, context, vetId, i);
        }

        log.info("Created schedules for {} veterinarians", vetIds.size());
    }

    private void createScheduleForVet(
            EntityManager entityManager, SeedContext context, UUID vetId, int vetIndex) {
        // Different vets have slightly different schedules
        for (var day : DayOfWeek.values()) {
            var schedule = createDaySchedule(vetId, day, vetIndex, context);
            entityManager.persist(schedule);
        }
    }

    private VeterinarianSchedule createDaySchedule(
            UUID vetId, DayOfWeek day, int vetIndex, SeedContext context) {
        var builder = VeterinarianSchedule.builder().veterinarianId(vetId).dayOfWeek(day);

        // Create varied schedules based on vet index
        switch (day) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY -> {
                builder.workingDay(true)
                        .startTime(vetIndex % 2 == 0 ? MORNING_START : LATE_START)
                        .endTime(vetIndex % 2 == 0 ? AFTERNOON_END : LocalTime.of(18, 0));
            }
            case FRIDAY -> {
                // Some vets work half day on Friday
                if (vetIndex % 3 == 0) {
                    builder.workingDay(true).startTime(MORNING_START).endTime(EARLY_END);
                } else if (vetIndex % 3 == 1) {
                    builder.workingDay(true).startTime(MORNING_START).endTime(AFTERNOON_END);
                } else {
                    builder.workingDay(false);
                }
            }
            case SATURDAY -> {
                // Half work on Saturday
                if (vetIndex % 2 == 0) {
                    builder.workingDay(true).startTime(SATURDAY_START).endTime(SATURDAY_END);
                } else {
                    builder.workingDay(false);
                }
            }
            case SUNDAY -> builder.workingDay(false);
        }

        var schedule = builder.build();
        schedule.setClinicId(context.getClinicId());
        return schedule;
    }

    private void createDaysOffForVet(
            EntityManager entityManager, SeedContext context, UUID vetId, int vetIndex) {
        var today = LocalDate.now();
        var random = context.getRandom();

        // Create some past days off
        if (vetIndex % 2 == 0) {
            var pastVacation =
                    createDayOff(
                            vetId,
                            today.minusWeeks(3),
                            today.minusWeeks(3).plusDays(4),
                            DayOffType.VACATION,
                            "Winter break",
                            true,
                            context);
            entityManager.persist(pastVacation);
        }

        // Create some future days off
        if (vetIndex % 3 == 0) {
            var futureVacation =
                    createDayOff(
                            vetId,
                            today.plusWeeks(2),
                            today.plusWeeks(2).plusDays(6),
                            DayOffType.VACATION,
                            "Planned vacation",
                            true,
                            context);
            entityManager.persist(futureVacation);
        }

        // Create a single day off for some vets
        if (vetIndex % 4 == 1) {
            var singleDayOff =
                    createDayOff(
                            vetId,
                            today.plusDays(5 + random.nextInt(10)),
                            today.plusDays(5 + random.nextInt(10)),
                            DayOffType.PERSONAL,
                            "Personal appointment",
                            true,
                            context);
            entityManager.persist(singleDayOff);
        }

        // Create an upcoming day off that's visible on the calendar this week
        if (vetIndex == 0) {
            var upcomingDayOff =
                    createDayOff(
                            vetId,
                            today.plusDays(1),
                            today.plusDays(2),
                            DayOffType.SICK_LEAVE,
                            "Medical appointment",
                            true,
                            context);
            entityManager.persist(upcomingDayOff);
        }
    }

    private VeterinarianDayOff createDayOff(
            UUID vetId,
            LocalDate startDate,
            LocalDate endDate,
            DayOffType type,
            String description,
            boolean approved,
            SeedContext context) {
        var dayOff =
                VeterinarianDayOff.builder()
                        .veterinarianId(vetId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .type(type)
                        .description(description)
                        .approved(approved)
                        .build();
        dayOff.setClinicId(context.getClinicId());
        return dayOff;
    }
}
