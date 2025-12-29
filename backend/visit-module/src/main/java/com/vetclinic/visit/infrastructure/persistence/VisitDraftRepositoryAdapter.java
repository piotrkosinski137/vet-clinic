package com.vetclinic.visit.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.visit.domain.model.VisitDraft;
import com.vetclinic.visit.domain.port.VisitDraftRepository;

import lombok.RequiredArgsConstructor;

/** Adapter implementing VisitDraftRepository using JPA. */
@Repository
@RequiredArgsConstructor
class VisitDraftRepositoryAdapter implements VisitDraftRepository {

    private final JpaVisitDraftRepository jpaRepository;

    @Override
    public Optional<VisitDraft> findByVisitId(UUID visitId) {
        return jpaRepository.findByVisitId(visitId);
    }

    @Override
    public VisitDraft save(VisitDraft draft) {
        return jpaRepository.save(draft);
    }

    @Override
    public void deleteByVisitId(UUID visitId) {
        jpaRepository.deleteByVisitId(visitId);
    }

    @Override
    public boolean existsByVisitId(UUID visitId) {
        return jpaRepository.existsByVisitId(visitId);
    }
}
