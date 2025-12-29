package com.vetclinic.visit.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vetclinic.visit.domain.model.VisitDraft;

/** JPA repository for visit drafts. */
public interface JpaVisitDraftRepository extends JpaRepository<VisitDraft, UUID> {

    /** Find a draft by visit ID. */
    Optional<VisitDraft> findByVisitId(UUID visitId);

    /** Delete a draft by visit ID. */
    @Modifying
    @Query("DELETE FROM VisitDraft d WHERE d.visitId = :visitId")
    void deleteByVisitId(@Param("visitId") UUID visitId);

    /** Check if a draft exists for a visit. */
    boolean existsByVisitId(UUID visitId);
}
