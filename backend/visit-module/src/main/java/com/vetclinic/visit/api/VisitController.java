package com.vetclinic.visit.api;

import static com.vetclinic.common.security.Roles.CAN_MANAGE_VISITS;
import static com.vetclinic.common.security.Roles.HAS_ANY_ROLE;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.vetclinic.visit.api.dto.CheckInRequest;
import com.vetclinic.visit.api.dto.VisitDraftDto;
import com.vetclinic.visit.api.dto.VisitReassignRequest;
import com.vetclinic.visit.api.dto.VisitRequest;
import com.vetclinic.visit.api.dto.VisitResponse;
import com.vetclinic.visit.api.dto.VisitStatusUpdateRequest;
import com.vetclinic.visit.api.dto.VisitSummaryResponse;
import com.vetclinic.visit.domain.VisitService;
import com.vetclinic.visit.domain.model.Visit;
import com.vetclinic.visit.domain.model.VisitStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;
    private final VisitMapper visitMapper;
    private final VisitDraftMapper draftMapper;

    @PostMapping
    @PreAuthorize(CAN_MANAGE_VISITS)
    public ResponseEntity<VisitResponse> createVisit(@Valid @RequestBody VisitRequest request) {
        Visit visit = visitMapper.toEntity(request);
        Visit created = visitService.createVisit(visit);
        VisitResponse response = visitMapper.toResponse(created);
        return ResponseEntity.created(URI.create("/api/v1/visits/" + created.getId()))
                .body(response);
    }

    // ===== WAITING ROOM ENDPOINTS =====
    // Note: These must be defined BEFORE /{id} to prevent path variable matching

    /** Get all visits currently in the waiting room. */
    @GetMapping("/waiting-room")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VisitResponse>> getWaitingRoom() {
        List<Visit> visits = visitService.getWaitingRoomVisits();
        return ResponseEntity.ok(visitMapper.toResponseList(visits));
    }

    /** Check in a patient to the waiting room. */
    @PostMapping("/{id}/check-in")
    @PreAuthorize(CAN_MANAGE_VISITS)
    public ResponseEntity<VisitResponse> checkIn(
            @PathVariable UUID id, @Valid @RequestBody(required = false) CheckInRequest request) {
        var notes = request != null ? request.waitingRoomNotes() : null;
        var priority = request != null ? request.priority() : null;
        Visit updated = visitService.checkInToWaitingRoom(id, notes, priority);
        return ResponseEntity.ok(visitMapper.toResponse(updated));
    }

    /** Start a visit from the waiting room. */
    @PostMapping("/{id}/start-from-waiting-room")
    @PreAuthorize(CAN_MANAGE_VISITS)
    public ResponseEntity<VisitResponse> startFromWaitingRoom(@PathVariable UUID id) {
        Visit updated = visitService.startVisitFromWaitingRoom(id);
        return ResponseEntity.ok(visitMapper.toResponse(updated));
    }

    /** Mark a visit as no-show. */
    @PostMapping("/{id}/no-show")
    @PreAuthorize(CAN_MANAGE_VISITS)
    public ResponseEntity<VisitResponse> markNoShow(@PathVariable UUID id) {
        Visit updated = visitService.markAsNoShow(id);
        return ResponseEntity.ok(visitMapper.toResponse(updated));
    }

    /** Update waiting room notes and priority. */
    @PatchMapping("/{id}/waiting-room-info")
    @PreAuthorize(CAN_MANAGE_VISITS)
    public ResponseEntity<VisitResponse> updateWaitingRoomInfo(
            @PathVariable UUID id, @Valid @RequestBody CheckInRequest request) {
        Visit updated =
                visitService.updateWaitingRoomInfo(
                        id, request.waitingRoomNotes(), request.priority());
        return ResponseEntity.ok(visitMapper.toResponse(updated));
    }

    @GetMapping("/{id}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<VisitResponse> getVisit(@PathVariable UUID id) {
        Visit visit = visitService.getVisit(id);
        return ResponseEntity.ok(visitMapper.toResponse(visit));
    }

    /**
     * Get all visits with optional filtering.
     *
     * @param patientId Filter by patient ID
     * @param clientId Filter by client ID
     * @param veterinarianId Filter by veterinarian ID
     * @param status Filter by visit status
     * @param dateFrom Filter visits from this date
     * @param dateTo Filter visits to this date
     */
    @GetMapping
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VisitResponse>> getAllVisits(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) UUID veterinarianId,
            @RequestParam(required = false) VisitStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate dateTo) {

        List<Visit> visits;
        if (veterinarianId != null && dateFrom != null && dateTo != null) {
            // Calendar view query - get visits for vet in date range
            visits = visitService.getVisitsForVeterinarian(veterinarianId, dateFrom, dateTo);
        } else if (veterinarianId != null) {
            visits = visitService.getVisitsByVeterinarian(veterinarianId);
        } else if (patientId != null
                || clientId != null
                || status != null
                || dateFrom != null
                || dateTo != null) {
            visits = visitService.searchVisits(patientId, clientId, status, dateFrom, dateTo);
        } else {
            visits = visitService.getAllVisits();
        }

        return ResponseEntity.ok(visitMapper.toResponseList(visits));
    }

    /** Get visit history for a specific patient. */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VisitResponse>> getPatientVisits(@PathVariable UUID patientId) {
        List<Visit> visits = visitService.getVisitsByPatient(patientId);
        return ResponseEntity.ok(visitMapper.toResponseList(visits));
    }

    /** Get visits for a specific date. */
    @GetMapping("/date/{date}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VisitResponse>> getVisitsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Visit> visits = visitService.getVisitsForDate(date);
        return ResponseEntity.ok(visitMapper.toResponseList(visits));
    }

    /** Get visits for a specific veterinarian on a specific date (calendar day view). */
    @GetMapping("/veterinarian/{veterinarianId}/date/{date}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VisitResponse>> getVeterinarianVisitsByDate(
            @PathVariable UUID veterinarianId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Visit> visits = visitService.getVisitsForVeterinarianOnDate(veterinarianId, date);
        return ResponseEntity.ok(visitMapper.toResponseList(visits));
    }

    /** Get all visits for a specific veterinarian. */
    @GetMapping("/veterinarian/{veterinarianId}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VisitResponse>> getVeterinarianVisits(
            @PathVariable UUID veterinarianId) {
        List<Visit> visits = visitService.getVisitsByVeterinarian(veterinarianId);
        return ResponseEntity.ok(visitMapper.toResponseList(visits));
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_VISITS)
    public ResponseEntity<VisitResponse> updateVisit(
            @PathVariable UUID id, @Valid @RequestBody VisitRequest request) {
        Visit visit = visitMapper.toEntity(request);
        Visit updated = visitService.updateVisit(id, visit);
        return ResponseEntity.ok(visitMapper.toResponse(updated));
    }

    /** Update visit status. */
    @PutMapping("/{id}/status")
    @PreAuthorize(CAN_MANAGE_VISITS)
    public ResponseEntity<VisitResponse> updateVisitStatus(
            @PathVariable UUID id, @Valid @RequestBody VisitStatusUpdateRequest request) {
        Visit updated = visitService.updateVisitStatus(id, request.status());
        return ResponseEntity.ok(visitMapper.toResponse(updated));
    }

    /** Reassign a visit to a different veterinarian and/or time slot (for drag-and-drop). */
    @PatchMapping("/{id}/reassign")
    @PreAuthorize(CAN_MANAGE_VISITS)
    public ResponseEntity<VisitResponse> reassignVisit(
            @PathVariable UUID id, @Valid @RequestBody VisitReassignRequest request) {
        Visit updated =
                visitService.reassignVisit(
                        id,
                        request.veterinarianId(),
                        request.veterinarianName(),
                        request.visitDate());
        return ResponseEntity.ok(visitMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_VISITS)
    public ResponseEntity<Void> deleteVisit(@PathVariable UUID id) {
        visitService.deleteVisit(id);
        return ResponseEntity.noContent().build();
    }

    /** Get printable visit summary. */
    @GetMapping("/{id}/summary")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<VisitSummaryResponse> getVisitSummary(@PathVariable UUID id) {
        Visit visit = visitService.getVisit(id);
        return ResponseEntity.ok(visitMapper.toSummaryResponse(visit));
    }

    // ===== DRAFT ENDPOINTS =====

    /**
     * Save or update a draft for a visit. Called automatically by frontend every few seconds while
     * editing.
     */
    @PutMapping("/{id}/draft")
    @PreAuthorize(CAN_MANAGE_VISITS)
    public ResponseEntity<VisitDraftDto> saveDraft(
            @PathVariable UUID id, @RequestBody VisitDraftDto draft) {
        // TODO: Get current user from SecurityContext
        var currentUser = "anonymous";
        var saved =
                visitService.saveDraft(
                        id,
                        draft.visitType(),
                        draft.interview(),
                        draft.examination(),
                        draft.diagnosis(),
                        draft.treatment(),
                        draft.recommendations(),
                        draft.weight(),
                        draft.temperature(),
                        draft.nextVisitDate(),
                        draftMapper.toUsedMaterialSnapshotList(draft.usedMaterials()),
                        draftMapper.toMedicationSnapshotList(draft.medications()),
                        currentUser);
        return ResponseEntity.ok(draftMapper.toDto(saved));
    }

    /**
     * Get existing draft for a visit. Called when opening VisitDetailsModal to restore unsaved
     * work.
     */
    @GetMapping("/{id}/draft")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<VisitDraftDto> getDraft(@PathVariable UUID id) {
        return visitService
                .getDraft(id)
                .map(draftMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete draft for a visit. Called after successful save (also done automatically by
     * updateVisit).
     */
    @DeleteMapping("/{id}/draft")
    @PreAuthorize(CAN_MANAGE_VISITS)
    public ResponseEntity<Void> deleteDraft(@PathVariable UUID id) {
        visitService.deleteDraft(id);
        return ResponseEntity.noContent().build();
    }
}
