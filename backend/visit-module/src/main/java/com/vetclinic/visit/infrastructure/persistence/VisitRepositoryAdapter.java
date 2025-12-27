package com.vetclinic.visit.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.vetclinic.visit.domain.model.Visit;
import com.vetclinic.visit.domain.model.VisitStatus;
import com.vetclinic.visit.domain.port.VisitRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class VisitRepositoryAdapter implements VisitRepository {

    private final JpaVisitRepository jpaRepository;

    @Override
    public Visit save(Visit visit) {
        return jpaRepository.save(visit);
    }

    @Override
    public Optional<Visit> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Visit> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Visit> findByPatientId(UUID patientId) {
        return jpaRepository.findByPatientId(patientId);
    }

    @Override
    public List<Visit> findByPatientIdOrderByVisitDateDesc(UUID patientId) {
        return jpaRepository.findByPatientIdOrderByVisitDateDesc(patientId);
    }

    @Override
    public List<Visit> findByClientId(UUID clientId) {
        return jpaRepository.findByClientId(clientId);
    }

    @Override
    public List<Visit> findByStatus(VisitStatus status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<Visit> findByVisitDateBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByVisitDateBetween(start, end);
    }

    @Override
    public List<Visit> findByPatientIdAndStatus(UUID patientId, VisitStatus status) {
        return jpaRepository.findByPatientIdAndStatus(patientId, status);
    }

    @Override
    public List<Visit> search(
            UUID patientId,
            UUID clientId,
            VisitStatus status,
            LocalDate dateFrom,
            LocalDate dateTo) {
        Specification<Visit> spec =
                (root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();

                    if (patientId != null) {
                        predicates.add(cb.equal(root.get("patientId"), patientId));
                    }
                    if (clientId != null) {
                        predicates.add(cb.equal(root.get("clientId"), clientId));
                    }
                    if (status != null) {
                        predicates.add(cb.equal(root.get("status"), status));
                    }
                    if (dateFrom != null) {
                        predicates.add(
                                cb.greaterThanOrEqualTo(
                                        root.get("visitDate"), dateFrom.atStartOfDay()));
                    }
                    if (dateTo != null) {
                        predicates.add(
                                cb.lessThan(
                                        root.get("visitDate"), dateTo.plusDays(1).atStartOfDay()));
                    }

                    return cb.and(predicates.toArray(new Predicate[0]));
                };

        return jpaRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "visitDate"));
    }

    @Override
    public List<Visit> findByVeterinarianIdAndVisitDateBetween(
            UUID veterinarianId, LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByVeterinarianIdAndVisitDateBetween(veterinarianId, start, end);
    }

    @Override
    public List<Visit> findByVeterinarianId(UUID veterinarianId) {
        return jpaRepository.findByVeterinarianId(veterinarianId);
    }

    @Override
    public boolean hasConflict(
            UUID veterinarianId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            UUID excludeVisitId) {
        return jpaRepository.hasConflict(veterinarianId, startTime, endTime, excludeVisitId);
    }
}
