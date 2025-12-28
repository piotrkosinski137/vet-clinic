package com.vetclinic.config;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vetclinic.veterinarian.domain.VeterinarianScheduleService;
import com.vetclinic.visit.domain.port.VeterinarianAvailabilityChecker;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of VeterinarianAvailabilityChecker that delegates to VeterinarianScheduleService.
 * This lives in the application layer to avoid circular dependencies between modules.
 */
@Component
@RequiredArgsConstructor
public class VeterinarianAvailabilityCheckerImpl implements VeterinarianAvailabilityChecker {

    private final VeterinarianScheduleService scheduleService;

    @Override
    public boolean isWorkingAt(UUID veterinarianId, LocalDateTime dateTime) {
        return scheduleService.isWorkingAt(veterinarianId, dateTime);
    }

    @Override
    public boolean isDayOff(UUID veterinarianId, LocalDateTime dateTime) {
        var availability = scheduleService.getAvailability(veterinarianId, dateTime.toLocalDate());
        return availability.isDayOff();
    }
}
