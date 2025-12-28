package com.vetclinic.veterinarian.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetclinic.veterinarian.api.dto.VeterinarianDayOffRequest;
import com.vetclinic.veterinarian.api.dto.VeterinarianScheduleRequest;
import com.vetclinic.veterinarian.api.dto.WeeklyScheduleRequest;
import com.vetclinic.veterinarian.domain.model.DayOffType;
import com.vetclinic.veterinarian.domain.model.Veterinarian;
import com.vetclinic.veterinarian.domain.model.VeterinarianDayOff;
import com.vetclinic.veterinarian.domain.model.VeterinarianSchedule;
import com.vetclinic.veterinarian.domain.port.VeterinarianDayOffRepository;
import com.vetclinic.veterinarian.domain.port.VeterinarianRepository;
import com.vetclinic.veterinarian.domain.port.VeterinarianScheduleRepository;
import com.vetclinic.veterinarian.domain.port.VeterinarianVisitChecker;

@ExtendWith(MockitoExtension.class)
@DisplayName("VeterinarianScheduleService")
class VeterinarianScheduleServiceTest {

    @Mock private VeterinarianScheduleRepository scheduleRepository;
    @Mock private VeterinarianDayOffRepository dayOffRepository;
    @Mock private VeterinarianRepository veterinarianRepository;
    @Mock private VeterinarianVisitChecker visitChecker;

    @Captor private ArgumentCaptor<List<VeterinarianSchedule>> schedulesCaptor;

    private VeterinarianScheduleService service;

    private UUID veterinarianId;
    private UUID clinicId;
    private Veterinarian veterinarian;

    @BeforeEach
    void setUp() {
        service =
                new VeterinarianScheduleService(
                        scheduleRepository, dayOffRepository, veterinarianRepository, visitChecker);
        veterinarianId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        veterinarian = createVeterinarian();
    }

    @Nested
    @DisplayName("Weekly Schedule Methods")
    class WeeklyScheduleTests {

        @Test
        @DisplayName("should get weekly schedule for veterinarian")
        void shouldGetWeeklySchedule() {
            // given
            var schedules = List.of(createSchedule(DayOfWeek.MONDAY, true, "08:00", "16:00"));
            given(veterinarianRepository.existsById(veterinarianId)).willReturn(true);
            given(scheduleRepository.findByVeterinarianId(veterinarianId)).willReturn(schedules);

            // when
            var result = service.getWeeklySchedule(veterinarianId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(result.getFirst().workingDay()).isTrue();
        }

        @Test
        @DisplayName("should throw when getting schedule for non-existent veterinarian")
        void shouldThrowWhenVetNotFound() {
            // given
            given(veterinarianRepository.existsById(veterinarianId)).willReturn(false);

            // when/then
            assertThatThrownBy(() -> service.getWeeklySchedule(veterinarianId))
                    .isInstanceOf(VeterinarianNotFoundException.class);
        }

        @Test
        @DisplayName("should update weekly schedule")
        void shouldUpdateWeeklySchedule() {
            // given
            var request =
                    new WeeklyScheduleRequest(
                            List.of(
                                    new VeterinarianScheduleRequest(
                                            DayOfWeek.MONDAY,
                                            LocalTime.of(9, 0),
                                            LocalTime.of(17, 0),
                                            true),
                                    new VeterinarianScheduleRequest(
                                            DayOfWeek.TUESDAY,
                                            LocalTime.of(9, 0),
                                            LocalTime.of(17, 0),
                                            true)));

            given(veterinarianRepository.findById(veterinarianId))
                    .willReturn(Optional.of(veterinarian));
            given(scheduleRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

            // when
            var result = service.updateWeeklySchedule(veterinarianId, request);

            // then
            verify(scheduleRepository).deleteByVeterinarianId(veterinarianId);
            verify(scheduleRepository).saveAll(schedulesCaptor.capture());

            var savedSchedules = schedulesCaptor.getValue();
            assertThat(savedSchedules).hasSize(2);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should get schedule for specific day")
        void shouldGetScheduleForDay() {
            // given
            var schedule = createSchedule(DayOfWeek.WEDNESDAY, true, "10:00", "18:00");
            given(
                            scheduleRepository.findByVeterinarianIdAndDayOfWeek(
                                    veterinarianId, DayOfWeek.WEDNESDAY))
                    .willReturn(Optional.of(schedule));

            // when
            var result = service.getScheduleForDay(veterinarianId, DayOfWeek.WEDNESDAY);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().dayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        }
    }

    @Nested
    @DisplayName("Days Off Methods")
    class DaysOffTests {

        @Test
        @DisplayName("should get all days off for veterinarian")
        void shouldGetDaysOff() {
            // given
            var dayOff =
                    createDayOff(LocalDate.now(), LocalDate.now().plusDays(5), DayOffType.VACATION);
            given(veterinarianRepository.existsById(veterinarianId)).willReturn(true);
            given(dayOffRepository.findByVeterinarianId(veterinarianId))
                    .willReturn(List.of(dayOff));

            // when
            var result = service.getDaysOff(veterinarianId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().type()).isEqualTo(DayOffType.VACATION);
        }

        @Test
        @DisplayName("should get days off in date range")
        void shouldGetDaysOffInRange() {
            // given
            var startDate = LocalDate.of(2024, 1, 1);
            var endDate = LocalDate.of(2024, 1, 31);
            var dayOff =
                    createDayOff(
                            LocalDate.of(2024, 1, 15),
                            LocalDate.of(2024, 1, 20),
                            DayOffType.SICK_LEAVE);

            given(veterinarianRepository.existsById(veterinarianId)).willReturn(true);
            given(
                            dayOffRepository.findByVeterinarianIdAndDateRange(
                                    veterinarianId, startDate, endDate))
                    .willReturn(List.of(dayOff));

            // when
            var result = service.getDaysOffInRange(veterinarianId, startDate, endDate);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().type()).isEqualTo(DayOffType.SICK_LEAVE);
        }

        @Test
        @DisplayName("should add day off")
        void shouldAddDayOff() {
            // given
            var request =
                    new VeterinarianDayOffRequest(
                            LocalDate.of(2024, 6, 1),
                            LocalDate.of(2024, 6, 10),
                            DayOffType.VACATION,
                            "Summer vacation");

            given(veterinarianRepository.findById(veterinarianId))
                    .willReturn(Optional.of(veterinarian));
            given(visitChecker.countVisitsInDateRange(any(), any(), any())).willReturn(0);
            given(dayOffRepository.save(any(VeterinarianDayOff.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // when
            var result = service.addDayOff(veterinarianId, request);

            // then
            assertThat(result.type()).isEqualTo(DayOffType.VACATION);
            assertThat(result.description()).isEqualTo("Summer vacation");
            assertThat(result.approved()).isFalse();
        }

        @Test
        @DisplayName("should throw when end date is before start date")
        void shouldThrowWhenInvalidDateRange() {
            // given
            var request =
                    new VeterinarianDayOffRequest(
                            LocalDate.of(2024, 6, 10),
                            LocalDate.of(2024, 6, 1), // End before start
                            DayOffType.VACATION,
                            null);

            given(veterinarianRepository.findById(veterinarianId))
                    .willReturn(Optional.of(veterinarian));

            // when/then
            assertThatThrownBy(() -> service.addDayOff(veterinarianId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("End date must not be before start date");
        }

        @Test
        @DisplayName("should update day off")
        void shouldUpdateDayOff() {
            // given
            var dayOffId = UUID.randomUUID();
            var existingDayOff =
                    createDayOff(
                            LocalDate.of(2024, 5, 1),
                            LocalDate.of(2024, 5, 5),
                            DayOffType.VACATION);
            existingDayOff.setId(dayOffId);

            var request =
                    new VeterinarianDayOffRequest(
                            LocalDate.of(2024, 5, 1),
                            LocalDate.of(2024, 5, 10),
                            DayOffType.PERSONAL,
                            "Extended time off");

            given(dayOffRepository.findById(dayOffId)).willReturn(Optional.of(existingDayOff));
            given(visitChecker.countVisitsInDateRange(any(), any(), any())).willReturn(0);
            given(dayOffRepository.save(any(VeterinarianDayOff.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // when
            var result = service.updateDayOff(dayOffId, request);

            // then
            assertThat(result.type()).isEqualTo(DayOffType.PERSONAL);
            assertThat(result.description()).isEqualTo("Extended time off");
        }

        @Test
        @DisplayName("should throw when adding day off with existing visits")
        void shouldThrowWhenAddingDayOffWithExistingVisits() {
            // given
            var request =
                    new VeterinarianDayOffRequest(
                            LocalDate.of(2024, 6, 1),
                            LocalDate.of(2024, 6, 10),
                            DayOffType.VACATION,
                            "Summer vacation");

            given(veterinarianRepository.findById(veterinarianId))
                    .willReturn(Optional.of(veterinarian));
            given(
                            visitChecker.countVisitsInDateRange(
                                    veterinarianId, request.startDate(), request.endDate()))
                    .willReturn(3);

            // when/then
            assertThatThrownBy(() -> service.addDayOff(veterinarianId, request))
                    .isInstanceOf(DayOffConflictException.class)
                    .hasMessageContaining("3 existing visit(s)");
        }

        @Test
        @DisplayName("should delete day off")
        void shouldDeleteDayOff() {
            // given
            var dayOffId = UUID.randomUUID();
            given(dayOffRepository.existsById(dayOffId)).willReturn(true);

            // when
            service.deleteDayOff(dayOffId);

            // then
            verify(dayOffRepository).deleteById(dayOffId);
        }

        @Test
        @DisplayName("should throw when deleting non-existent day off")
        void shouldThrowWhenDeletingNonExistentDayOff() {
            // given
            var dayOffId = UUID.randomUUID();
            given(dayOffRepository.existsById(dayOffId)).willReturn(false);

            // when/then
            assertThatThrownBy(() -> service.deleteDayOff(dayOffId))
                    .isInstanceOf(DayOffNotFoundException.class);
        }

        @Test
        @DisplayName("should approve day off")
        void shouldApproveDayOff() {
            // given
            var dayOffId = UUID.randomUUID();
            var dayOff =
                    createDayOff(LocalDate.now(), LocalDate.now().plusDays(3), DayOffType.VACATION);
            dayOff.setId(dayOffId);
            dayOff.setApproved(false);

            given(dayOffRepository.findById(dayOffId)).willReturn(Optional.of(dayOff));
            given(dayOffRepository.save(any(VeterinarianDayOff.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // when
            var result = service.approveDayOff(dayOffId, true);

            // then
            assertThat(result.approved()).isTrue();
        }
    }

    @Nested
    @DisplayName("Availability Methods")
    class AvailabilityTests {

        @Test
        @DisplayName("should return availability for working day")
        void shouldReturnAvailabilityForWorkingDay() {
            // given
            var date = LocalDate.of(2024, 6, 3); // Monday
            var schedule = createSchedule(DayOfWeek.MONDAY, true, "09:00", "17:00");

            given(veterinarianRepository.existsById(veterinarianId)).willReturn(true);
            given(
                            scheduleRepository.findByVeterinarianIdAndDayOfWeek(
                                    veterinarianId, DayOfWeek.MONDAY))
                    .willReturn(Optional.of(schedule));
            given(dayOffRepository.findByVeterinarianIdAndDate(veterinarianId, date))
                    .willReturn(Collections.emptyList());

            // when
            var result = service.getAvailability(veterinarianId, date);

            // then
            assertThat(result.workingDay()).isTrue();
            assertThat(result.isDayOff()).isFalse();
            assertThat(result.startTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(result.endTime()).isEqualTo(LocalTime.of(17, 0));
        }

        @Test
        @DisplayName("should return availability for day off")
        void shouldReturnAvailabilityForDayOff() {
            // given
            var date = LocalDate.of(2024, 6, 3);
            var schedule = createSchedule(DayOfWeek.MONDAY, true, "09:00", "17:00");
            var dayOff = createDayOff(date, date.plusDays(2), DayOffType.VACATION);

            given(veterinarianRepository.existsById(veterinarianId)).willReturn(true);
            given(
                            scheduleRepository.findByVeterinarianIdAndDayOfWeek(
                                    veterinarianId, DayOfWeek.MONDAY))
                    .willReturn(Optional.of(schedule));
            given(dayOffRepository.findByVeterinarianIdAndDate(veterinarianId, date))
                    .willReturn(List.of(dayOff));

            // when
            var result = service.getAvailability(veterinarianId, date);

            // then
            assertThat(result.workingDay()).isFalse();
            assertThat(result.isDayOff()).isTrue();
            assertThat(result.dayOffType()).isEqualTo(DayOffType.VACATION);
        }

        @Test
        @DisplayName("should return availability for non-working day")
        void shouldReturnAvailabilityForNonWorkingDay() {
            // given
            var date = LocalDate.of(2024, 6, 8); // Saturday
            var schedule = createSchedule(DayOfWeek.SATURDAY, false, null, null);

            given(veterinarianRepository.existsById(veterinarianId)).willReturn(true);
            given(
                            scheduleRepository.findByVeterinarianIdAndDayOfWeek(
                                    veterinarianId, DayOfWeek.SATURDAY))
                    .willReturn(Optional.of(schedule));
            given(dayOffRepository.findByVeterinarianIdAndDate(veterinarianId, date))
                    .willReturn(Collections.emptyList());

            // when
            var result = service.getAvailability(veterinarianId, date);

            // then
            assertThat(result.workingDay()).isFalse();
            assertThat(result.isDayOff()).isFalse();
        }

        @Test
        @DisplayName("should use default hours when no schedule exists")
        void shouldUseDefaultHoursWhenNoSchedule() {
            // given
            var date = LocalDate.of(2024, 6, 3); // Monday

            given(veterinarianRepository.existsById(veterinarianId)).willReturn(true);
            given(
                            scheduleRepository.findByVeterinarianIdAndDayOfWeek(
                                    veterinarianId, DayOfWeek.MONDAY))
                    .willReturn(Optional.empty());
            given(dayOffRepository.findByVeterinarianIdAndDate(veterinarianId, date))
                    .willReturn(Collections.emptyList());

            // when
            var result = service.getAvailability(veterinarianId, date);

            // then
            assertThat(result.workingDay()).isTrue();
            assertThat(result.startTime()).isEqualTo(LocalTime.of(8, 0));
            assertThat(result.endTime()).isEqualTo(LocalTime.of(17, 0));
        }

        @Test
        @DisplayName("should check if working at specific datetime")
        void shouldCheckIfWorkingAtDatetime() {
            // given
            var dateTime = LocalDateTime.of(2024, 6, 3, 10, 0); // Monday 10:00
            var schedule = createSchedule(DayOfWeek.MONDAY, true, "09:00", "17:00");

            given(
                            dayOffRepository.findByVeterinarianIdAndDate(
                                    veterinarianId, dateTime.toLocalDate()))
                    .willReturn(Collections.emptyList());
            given(
                            scheduleRepository.findByVeterinarianIdAndDayOfWeek(
                                    veterinarianId, DayOfWeek.MONDAY))
                    .willReturn(Optional.of(schedule));

            // when
            var result = service.isWorkingAt(veterinarianId, dateTime);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when on day off")
        void shouldReturnFalseWhenOnDayOff() {
            // given
            var dateTime = LocalDateTime.of(2024, 6, 3, 10, 0);
            var dayOff =
                    createDayOff(
                            dateTime.toLocalDate(),
                            dateTime.toLocalDate().plusDays(1),
                            DayOffType.SICK_LEAVE);

            given(
                            dayOffRepository.findByVeterinarianIdAndDate(
                                    veterinarianId, dateTime.toLocalDate()))
                    .willReturn(List.of(dayOff));

            // when
            var result = service.isWorkingAt(veterinarianId, dateTime);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when outside working hours")
        void shouldReturnFalseWhenOutsideWorkingHours() {
            // given
            var dateTime = LocalDateTime.of(2024, 6, 3, 7, 0); // 7 AM - before work
            var schedule = createSchedule(DayOfWeek.MONDAY, true, "09:00", "17:00");

            given(
                            dayOffRepository.findByVeterinarianIdAndDate(
                                    veterinarianId, dateTime.toLocalDate()))
                    .willReturn(Collections.emptyList());
            given(
                            scheduleRepository.findByVeterinarianIdAndDayOfWeek(
                                    veterinarianId, DayOfWeek.MONDAY))
                    .willReturn(Optional.of(schedule));

            // when
            var result = service.isWorkingAt(veterinarianId, dateTime);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Initialization Methods")
    class InitializationTests {

        @Test
        @DisplayName("should initialize default schedule for new veterinarian")
        void shouldInitializeDefaultSchedule() {
            // given
            given(scheduleRepository.existsByVeterinarianId(veterinarianId)).willReturn(false);
            given(veterinarianRepository.findById(veterinarianId))
                    .willReturn(Optional.of(veterinarian));
            given(scheduleRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

            // when
            service.initializeDefaultSchedule(veterinarianId);

            // then
            verify(scheduleRepository).saveAll(schedulesCaptor.capture());
            var savedSchedules = schedulesCaptor.getValue();

            assertThat(savedSchedules).hasSize(7);

            // Verify weekdays are working days
            var monday =
                    savedSchedules.stream()
                            .filter(s -> s.getDayOfWeek() == DayOfWeek.MONDAY)
                            .findFirst()
                            .orElseThrow();
            assertThat(monday.isWorkingDay()).isTrue();
            assertThat(monday.getStartTime()).isEqualTo(LocalTime.of(8, 0));

            // Verify weekend is not working
            var saturday =
                    savedSchedules.stream()
                            .filter(s -> s.getDayOfWeek() == DayOfWeek.SATURDAY)
                            .findFirst()
                            .orElseThrow();
            assertThat(saturday.isWorkingDay()).isFalse();
        }

        @Test
        @DisplayName("should not reinitialize if schedule already exists")
        void shouldNotReinitializeIfScheduleExists() {
            // given
            given(scheduleRepository.existsByVeterinarianId(veterinarianId)).willReturn(true);

            // when
            service.initializeDefaultSchedule(veterinarianId);

            // then
            verify(scheduleRepository, never()).saveAll(anyList());
        }
    }

    // ==================== Helper Methods ====================

    private Veterinarian createVeterinarian() {
        var vet =
                Veterinarian.builder()
                        .firstName("Jan")
                        .lastName("Kowalski")
                        .email("jan.kowalski@vetclinic.pl")
                        .active(true)
                        .build();
        vet.setId(veterinarianId);
        vet.setClinicId(clinicId);
        return vet;
    }

    private VeterinarianSchedule createSchedule(
            DayOfWeek dayOfWeek, boolean workingDay, String startTime, String endTime) {
        var schedule =
                VeterinarianSchedule.builder()
                        .veterinarianId(veterinarianId)
                        .dayOfWeek(dayOfWeek)
                        .workingDay(workingDay)
                        .startTime(startTime != null ? LocalTime.parse(startTime) : null)
                        .endTime(endTime != null ? LocalTime.parse(endTime) : null)
                        .build();
        schedule.setId(UUID.randomUUID());
        schedule.setClinicId(clinicId);
        return schedule;
    }

    private VeterinarianDayOff createDayOff(
            LocalDate startDate, LocalDate endDate, DayOffType type) {
        var dayOff =
                VeterinarianDayOff.builder()
                        .veterinarianId(veterinarianId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .type(type)
                        .approved(false)
                        .build();
        dayOff.setId(UUID.randomUUID());
        dayOff.setClinicId(clinicId);
        return dayOff;
    }
}
