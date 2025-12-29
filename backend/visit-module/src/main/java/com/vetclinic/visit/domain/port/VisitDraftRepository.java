package com.vetclinic.visit.domain.port;

import java.util.Optional;
import java.util.UUID;

import com.vetclinic.visit.domain.model.VisitDraft;

/** Repository port for visit drafts. Handles persistence of temporary visit form data. */
public interface VisitDraftRepository {

    /**
     * Find a draft by visit ID.
     *
     * @param visitId the visit ID
     * @return the draft if it exists
     */
    Optional<VisitDraft> findByVisitId(UUID visitId);

    /**
     * Save or update a draft.
     *
     * @param draft the draft to save
     * @return the saved draft
     */
    VisitDraft save(VisitDraft draft);

    /**
     * Delete a draft by visit ID.
     *
     * @param visitId the visit ID
     */
    void deleteByVisitId(UUID visitId);

    /**
     * Check if a draft exists for a visit.
     *
     * @param visitId the visit ID
     * @return true if a draft exists
     */
    boolean existsByVisitId(UUID visitId);
}
