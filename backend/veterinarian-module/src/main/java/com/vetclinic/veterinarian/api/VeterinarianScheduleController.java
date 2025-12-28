package com.vetclinic.veterinarian.api;

import static com.vetclinic.common.security.Roles.CAN_MANAGE_VETS;
import static com.vetclinic.common.security.Roles.HAS_ANY_ROLE;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.veterinarian.api.dto.VeterinarianAvailabilityResponse;
import com.vetclinic.veterinarian.api.dto.VeterinarianDayOffRequest;
import com.vetclinic.veterinarian.api.dto.VeterinarianDayOffResponse;
import com.vetclinic.veterinarian.api.dto.VeterinarianScheduleResponse;
import com.vetclinic.veterinarian.api.dto.WeeklyScheduleRequest;
import com.vetclinic.veterinarian.domain.VeterinarianScheduleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/veterinarians")
@RequiredArgsConstructor
public class VeterinarianScheduleController {

    private final VeterinarianScheduleService scheduleService;

    // ==================== Weekly Schedule Endpoints ====================

    @GetMapping("/{id}/schedule")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VeterinarianScheduleResponse>> getWeeklySchedule(
            @PathVariable UUID id) {
        return ResponseEntity.ok(scheduleService.getWeeklySchedule(id));
    }

    @PutMapping("/{id}/schedule")
    @PreAuthorize(CAN_MANAGE_VETS)
    public ResponseEntity<List<VeterinarianScheduleResponse>> updateWeeklySchedule(
            @PathVariable UUID id, @Valid @RequestBody WeeklyScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.updateWeeklySchedule(id, request));
    }

    // ==================== Days Off Endpoints ====================

    @GetMapping("/{id}/days-off")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VeterinarianDayOffResponse>> getDaysOff(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(scheduleService.getDaysOffInRange(id, startDate, endDate));
        }
        return ResponseEntity.ok(scheduleService.getDaysOff(id));
    }

    @PostMapping("/{id}/days-off")
    @PreAuthorize(CAN_MANAGE_VETS)
    public ResponseEntity<VeterinarianDayOffResponse> addDayOff(
            @PathVariable UUID id, @Valid @RequestBody VeterinarianDayOffRequest request) {
        var dayOff = scheduleService.addDayOff(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dayOff);
    }

    @PutMapping("/{id}/days-off/{dayOffId}")
    @PreAuthorize(CAN_MANAGE_VETS)
    public ResponseEntity<VeterinarianDayOffResponse> updateDayOff(
            @PathVariable UUID id,
            @PathVariable UUID dayOffId,
            @Valid @RequestBody VeterinarianDayOffRequest request) {
        return ResponseEntity.ok(scheduleService.updateDayOff(dayOffId, request));
    }

    @DeleteMapping("/{id}/days-off/{dayOffId}")
    @PreAuthorize(CAN_MANAGE_VETS)
    public ResponseEntity<Void> deleteDayOff(@PathVariable UUID id, @PathVariable UUID dayOffId) {
        scheduleService.deleteDayOff(dayOffId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/days-off/{dayOffId}/approve")
    @PreAuthorize(CAN_MANAGE_VETS)
    public ResponseEntity<VeterinarianDayOffResponse> approveDayOff(
            @PathVariable UUID id, @PathVariable UUID dayOffId, @RequestParam boolean approved) {
        return ResponseEntity.ok(scheduleService.approveDayOff(dayOffId, approved));
    }

    // ==================== Availability Endpoints ====================

    @GetMapping("/{id}/availability")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<VeterinarianAvailabilityResponse> getAvailability(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(scheduleService.getAvailability(id, date));
    }

    @GetMapping("/availability")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VeterinarianAvailabilityResponse>> getAllAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(scheduleService.getAllAvailability(date));
    }
}
