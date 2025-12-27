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
}
