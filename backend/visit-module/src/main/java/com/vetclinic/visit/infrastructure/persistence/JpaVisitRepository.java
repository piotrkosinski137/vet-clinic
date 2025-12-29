package com.vetclinic.visit.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vetclinic.visit.domain.model.Visit;
import com.vetclinic.visit.domain.model.VisitStatus;

public interface JpaVisitRepository
        extends JpaRepository<Visit, UUID>, JpaSpecificationExecutor<Visit> {

    List<Visit> findByPatientId(UUID patientId);

    List<Visit> findByPatientIdOrderByVisitDateDesc(UUID patientId);

    List<Visit> findByClientId(UUID clientId);

    List<Visit> findByStatus(VisitStatus status);

    List<Visit> findByVisitDateBetween(LocalDateTime start, LocalDateTime end);

    List<Visit> findByPatientIdAndStatus(UUID patientId, VisitStatus status);

    List<Visit> findByVeterinarianId(UUID veterinarianId);

    List<Visit> findByVeterinarianIdAndVisitDateBetween(
            UUID veterinarianId, LocalDateTime start, LocalDateTime end);

    @Query(
            value =
                    """
            SELECT COUNT(*) > 0 FROM visits v
            WHERE v.veterinarian_id = :veterinarianId
            AND v.status <> 'CANCELLED'
            AND (:excludeVisitId IS NULL OR v.id <> :excludeVisitId)
            AND v.visit_date < :endTime
            AND (v.visit_date + (v.duration_minutes * INTERVAL '1 minute')) > :startTime
            """,
            nativeQuery = true)
    boolean hasConflict(
            @Param("veterinarianId") UUID veterinarianId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeVisitId") UUID excludeVisitId);

    /**
     * Find all visits currently in the waiting room. Ordered by priority (URGENT first) then by
     * check-in time (earliest first).
     */
    @Query(
            """
            SELECT v FROM Visit v
            WHERE v.status = 'CHECKED_IN'
            AND v.visitDate >= :startOfDay
            AND v.visitDate < :endOfDay
            ORDER BY
                CASE v.priority
                    WHEN com.vetclinic.visit.domain.model.VisitPriority.URGENT THEN 1
                    WHEN com.vetclinic.visit.domain.model.VisitPriority.HIGH THEN 2
                    WHEN com.vetclinic.visit.domain.model.VisitPriority.NORMAL THEN 3
                    WHEN com.vetclinic.visit.domain.model.VisitPriority.LOW THEN 4
                    ELSE 5
                END,
                v.checkedInAt ASC
            """)
    List<Visit> findWaitingRoomVisits(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    List<Visit> findByStatusAndVisitDateBetween(
            VisitStatus status, LocalDateTime start, LocalDateTime end);
}
