package com.vetclinic.visit.domain.port;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.visit.domain.model.Visit;
import com.vetclinic.visit.domain.model.VisitStatus;

/** Port for visit persistence operations. This interface is implemented by infrastructure layer. */
public interface VisitRepository {

    Visit save(Visit visit);

    Optional<Visit> findById(UUID id);

    List<Visit> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);

    /** Find all visits for a specific patient */
    List<Visit> findByPatientId(UUID patientId);

    /** Find all visits for a specific patient, ordered by visit date descending */
    List<Visit> findByPatientIdOrderByVisitDateDesc(UUID patientId);

    /** Find all visits for a specific client */
    List<Visit> findByClientId(UUID clientId);

    /** Find visits by status */
    List<Visit> findByStatus(VisitStatus status);

    /** Find visits scheduled for a specific date */
    List<Visit> findByVisitDateBetween(LocalDateTime start, LocalDateTime end);

    /** Find visits for a patient with a specific status */
    List<Visit> findByPatientIdAndStatus(UUID patientId, VisitStatus status);

    /** Search visits by multiple criteria */
    List<Visit> search(
            UUID patientId,
            UUID clientId,
            VisitStatus status,
            LocalDate dateFrom,
            LocalDate dateTo);

    /** Find visits for a specific veterinarian on a specific date */
    List<Visit> findByVeterinarianIdAndVisitDateBetween(
            UUID veterinarianId, LocalDateTime start, LocalDateTime end);

    /** Find visits for a specific veterinarian */
    List<Visit> findByVeterinarianId(UUID veterinarianId);

    /**
     * Check if there is a conflicting appointment for a veterinarian. Excludes cancelled visits and
     * optionally excludes a specific visit (for updates).
     */
    boolean hasConflict(
            UUID veterinarianId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            UUID excludeVisitId);

    /**
     * Find all visits currently in the waiting room (CHECKED_IN status) for today. Results are
     * ordered by priority (URGENT first) then by check-in time (earliest first).
     */
    List<Visit> findWaitingRoomVisits(LocalDateTime startOfDay, LocalDateTime endOfDay);

    /** Find visits by status within a date range */
    List<Visit> findByStatusAndVisitDateBetween(
            VisitStatus status, LocalDateTime start, LocalDateTime end);
}
