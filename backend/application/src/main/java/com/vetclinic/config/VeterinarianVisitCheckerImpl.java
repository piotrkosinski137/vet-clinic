package com.vetclinic.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vetclinic.veterinarian.domain.port.VeterinarianVisitChecker;
import com.vetclinic.visit.domain.port.VisitRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of VeterinarianVisitChecker that queries the visit repository. This lives in the
 * application layer to avoid circular dependencies between veterinarian-module and visit-module.
 */
@Component
@RequiredArgsConstructor
public class VeterinarianVisitCheckerImpl implements VeterinarianVisitChecker {

    private final VisitRepository visitRepository;

    @Override
    public int countVisitsInDateRange(UUID veterinarianId, LocalDate startDate, LocalDate endDate) {
        var startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);
        var endDateTime = LocalDateTime.of(endDate, LocalTime.MAX);

        var visits =
                visitRepository.findByVeterinarianIdAndVisitDateBetween(
                        veterinarianId, startDateTime, endDateTime);

        // Filter out cancelled visits
        return (int)
                visits.stream()
                        .filter(
                                v ->
                                        v.getStatus()
                                                != com.vetclinic.visit.domain.model.VisitStatus
                                                        .CANCELLED)
                        .count();
    }
}
