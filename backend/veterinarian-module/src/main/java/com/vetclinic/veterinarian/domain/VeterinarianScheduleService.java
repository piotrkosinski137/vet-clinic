package com.vetclinic.veterinarian.domain;

import static com.vetclinic.common.constants.AppConstants.DEFAULT_WORK_END_TIME;
import static com.vetclinic.common.constants.AppConstants.DEFAULT_WORK_START_TIME;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.veterinarian.api.dto.VeterinarianAvailabilityResponse;
import com.vetclinic.veterinarian.api.dto.VeterinarianDayOffRequest;
import com.vetclinic.veterinarian.api.dto.VeterinarianDayOffResponse;
import com.vetclinic.veterinarian.api.dto.VeterinarianScheduleResponse;
import com.vetclinic.veterinarian.api.dto.WeeklyScheduleRequest;
import com.vetclinic.veterinarian.domain.model.VeterinarianDayOff;
import com.vetclinic.veterinarian.domain.model.VeterinarianSchedule;
import com.vetclinic.veterinarian.domain.port.VeterinarianDayOffRepository;
import com.vetclinic.veterinarian.domain.port.VeterinarianRepository;
import com.vetclinic.veterinarian.domain.port.VeterinarianScheduleRepository;
import com.vetclinic.veterinarian.domain.port.VeterinarianVisitChecker;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VeterinarianScheduleService {

    private final VeterinarianScheduleRepository scheduleRepository;
    private final VeterinarianDayOffRepository dayOffRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final VeterinarianVisitChecker visitChecker;

    // ==================== Weekly Schedule Methods ====================

    public List<VeterinarianScheduleResponse> getWeeklySchedule(UUID veterinarianId) {
        validateVeterinarianExists(veterinarianId);
        return scheduleRepository.findByVeterinarianId(veterinarianId).stream()
                .map(this::toScheduleResponse)
                .toList();
    }

    @Transactional
    public List<VeterinarianScheduleResponse> updateWeeklySchedule(
            UUID veterinarianId, WeeklyScheduleRequest request) {
        var veterinarian =
                veterinarianRepository
                        .findById(veterinarianId)
                        .orElseThrow(() -> new VeterinarianNotFoundException(veterinarianId));

        // Delete existing schedules
        scheduleRepository.deleteByVeterinarianId(veterinarianId);

        // Create new schedules
        var schedules = new ArrayList<VeterinarianSchedule>();
        for (var scheduleReq : request.schedules()) {
            var schedule =
                    VeterinarianSchedule.builder()
                            .veterinarianId(veterinarianId)
                            .dayOfWeek(scheduleReq.dayOfWeek())
                            .startTime(scheduleReq.startTime())
                            .endTime(scheduleReq.endTime())
                            .workingDay(scheduleReq.workingDay())
                            .build();
            schedule.setClinicId(veterinarian.getClinicId());
            schedules.add(schedule);
        }

        return scheduleRepository.saveAll(schedules).stream()
                .map(this::toScheduleResponse)
                .toList();
    }

    public Optional<VeterinarianScheduleResponse> getScheduleForDay(
            UUID veterinarianId, DayOfWeek dayOfWeek) {
        return scheduleRepository
                .findByVeterinarianIdAndDayOfWeek(veterinarianId, dayOfWeek)
                .map(this::toScheduleResponse);
    }

    // ==================== Days Off Methods ====================

    public List<VeterinarianDayOffResponse> getDaysOff(UUID veterinarianId) {
        validateVeterinarianExists(veterinarianId);
        return dayOffRepository.findByVeterinarianId(veterinarianId).stream()
                .map(this::toDayOffResponse)
                .toList();
    }

    public List<VeterinarianDayOffResponse> getDaysOffInRange(
            UUID veterinarianId, LocalDate startDate, LocalDate endDate) {
        validateVeterinarianExists(veterinarianId);
        return dayOffRepository
                .findByVeterinarianIdAndDateRange(veterinarianId, startDate, endDate)
                .stream()
                .map(this::toDayOffResponse)
                .toList();
    }

    @Transactional
    public VeterinarianDayOffResponse addDayOff(
            UUID veterinarianId, VeterinarianDayOffRequest request) {
        var veterinarian =
                veterinarianRepository
                        .findById(veterinarianId)
                        .orElseThrow(() -> new VeterinarianNotFoundException(veterinarianId));

        validateDayOffDates(request.startDate(), request.endDate());
        validateNoExistingVisits(veterinarianId, request.startDate(), request.endDate());

        var dayOff =
                VeterinarianDayOff.builder()
                        .veterinarianId(veterinarianId)
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .type(request.type())
                        .description(request.description())
                        .approved(false)
                        .build();
        dayOff.setClinicId(veterinarian.getClinicId());

        return toDayOffResponse(dayOffRepository.save(dayOff));
    }

    @Transactional
    public VeterinarianDayOffResponse updateDayOff(
            UUID dayOffId, VeterinarianDayOffRequest request) {
        var dayOff =
                dayOffRepository
                        .findById(dayOffId)
                        .orElseThrow(() -> new DayOffNotFoundException(dayOffId));

        validateDayOffDates(request.startDate(), request.endDate());
        validateNoExistingVisits(
                dayOff.getVeterinarianId(), request.startDate(), request.endDate());

        dayOff.setStartDate(request.startDate());
        dayOff.setEndDate(request.endDate());
        dayOff.setType(request.type());
        dayOff.setDescription(request.description());

        return toDayOffResponse(dayOffRepository.save(dayOff));
    }

    @Transactional
    public void deleteDayOff(UUID dayOffId) {
        if (!dayOffRepository.existsById(dayOffId)) {
            throw new DayOffNotFoundException(dayOffId);
        }
        dayOffRepository.deleteById(dayOffId);
    }

    @Transactional
    public VeterinarianDayOffResponse approveDayOff(UUID dayOffId, boolean approved) {
        var dayOff =
                dayOffRepository
                        .findById(dayOffId)
                        .orElseThrow(() -> new DayOffNotFoundException(dayOffId));

        dayOff.setApproved(approved);
        return toDayOffResponse(dayOffRepository.save(dayOff));
    }

    // ==================== Availability Methods ====================

    public VeterinarianAvailabilityResponse getAvailability(UUID veterinarianId, LocalDate date) {
        validateVeterinarianExists(veterinarianId);

        var dayOfWeek = date.getDayOfWeek();
        var schedule =
                scheduleRepository.findByVeterinarianIdAndDayOfWeek(veterinarianId, dayOfWeek);
        var daysOff = dayOffRepository.findByVeterinarianIdAndDate(veterinarianId, date);

        var isDayOff = !daysOff.isEmpty();
        var dayOff = isDayOff ? daysOff.getFirst() : null;

        if (schedule.isEmpty()) {
            // No schedule defined - assume working day with default hours
            return VeterinarianAvailabilityResponse.builder()
                    .veterinarianId(veterinarianId)
                    .date(date)
                    .workingDay(!isDayOff)
                    .startTime(isDayOff ? null : DEFAULT_WORK_START_TIME)
                    .endTime(isDayOff ? null : DEFAULT_WORK_END_TIME)
                    .isDayOff(isDayOff)
                    .dayOffType(isDayOff ? dayOff.getType() : null)
                    .dayOffDescription(isDayOff ? dayOff.getDescription() : null)
                    .build();
        }

        var sched = schedule.get();
        return VeterinarianAvailabilityResponse.builder()
                .veterinarianId(veterinarianId)
                .date(date)
                .workingDay(sched.isWorkingDay() && !isDayOff)
                .startTime(isDayOff ? null : sched.getStartTime())
                .endTime(isDayOff ? null : sched.getEndTime())
                .isDayOff(isDayOff)
                .dayOffType(isDayOff ? dayOff.getType() : null)
                .dayOffDescription(isDayOff ? dayOff.getDescription() : null)
                .build();
    }

    public List<VeterinarianAvailabilityResponse> getAllAvailability(LocalDate date) {
        return veterinarianRepository.findByActive(true).stream()
                .map(vet -> getAvailability(vet.getId(), date))
                .toList();
    }

    public boolean isWorkingAt(UUID veterinarianId, LocalDateTime dateTime) {
        var date = dateTime.toLocalDate();
        var time = dateTime.toLocalTime();

        // Check for day off
        var daysOff = dayOffRepository.findByVeterinarianIdAndDate(veterinarianId, date);
        if (!daysOff.isEmpty()) {
            return false;
        }

        // Check schedule
        var schedule =
                scheduleRepository.findByVeterinarianIdAndDayOfWeek(
                        veterinarianId, date.getDayOfWeek());
        if (schedule.isEmpty()) {
            // No schedule - assume default working hours
            return !time.isBefore(DEFAULT_WORK_START_TIME) && time.isBefore(DEFAULT_WORK_END_TIME);
        }

        return schedule.get().isWithinWorkingHours(time);
    }

    // ==================== Initialization ====================

    @Transactional
    public void initializeDefaultSchedule(UUID veterinarianId) {
        if (scheduleRepository.existsByVeterinarianId(veterinarianId)) {
            return; // Already has schedule
        }

        var veterinarian =
                veterinarianRepository
                        .findById(veterinarianId)
                        .orElseThrow(() -> new VeterinarianNotFoundException(veterinarianId));

        var schedules = new ArrayList<VeterinarianSchedule>();
        for (var day : DayOfWeek.values()) {
            var isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
            var schedule =
                    VeterinarianSchedule.builder()
                            .veterinarianId(veterinarianId)
                            .dayOfWeek(day)
                            .workingDay(!isWeekend)
                            .startTime(isWeekend ? null : DEFAULT_WORK_START_TIME)
                            .endTime(isWeekend ? null : DEFAULT_WORK_END_TIME)
                            .build();
            schedule.setClinicId(veterinarian.getClinicId());
            schedules.add(schedule);
        }
        scheduleRepository.saveAll(schedules);
    }

    // ==================== Helper Methods ====================

    private void validateVeterinarianExists(UUID veterinarianId) {
        if (!veterinarianRepository.existsById(veterinarianId)) {
            throw new VeterinarianNotFoundException(veterinarianId);
        }
    }

    private void validateDayOffDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must not be before start date");
        }
    }

    private void validateNoExistingVisits(
            UUID veterinarianId, LocalDate startDate, LocalDate endDate) {
        var visitCount = visitChecker.countVisitsInDateRange(veterinarianId, startDate, endDate);
        if (visitCount > 0) {
            throw new DayOffConflictException(veterinarianId, startDate, endDate, visitCount);
        }
    }

    private VeterinarianScheduleResponse toScheduleResponse(VeterinarianSchedule schedule) {
        return VeterinarianScheduleResponse.builder()
                .id(schedule.getId())
                .veterinarianId(schedule.getVeterinarianId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .workingDay(schedule.isWorkingDay())
                .build();
    }

    private VeterinarianDayOffResponse toDayOffResponse(VeterinarianDayOff dayOff) {
        return VeterinarianDayOffResponse.builder()
                .id(dayOff.getId())
                .veterinarianId(dayOff.getVeterinarianId())
                .startDate(dayOff.getStartDate())
                .endDate(dayOff.getEndDate())
                .type(dayOff.getType())
                .description(dayOff.getDescription())
                .approved(dayOff.isApproved())
                .createdAt(dayOff.getCreatedAt())
                .build();
    }
}
