package com.vetclinic.visit.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.common.constants.AppConstants;
import com.vetclinic.common.event.DomainEventPublisher;
import com.vetclinic.common.util.ChangeDetector;
import com.vetclinic.common.util.DateTimeRange;
import com.vetclinic.visit.domain.model.Visit;
import com.vetclinic.visit.domain.model.VisitPriority;
import com.vetclinic.visit.domain.model.VisitStatus;
import com.vetclinic.visit.domain.port.VeterinarianAvailabilityChecker;
import com.vetclinic.visit.domain.port.VisitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitService {

    private static final String ENTITY_TYPE = "Visit";

    private final VisitRepository visitRepository;
    private final DomainEventPublisher eventPublisher;
    private final VeterinarianAvailabilityChecker availabilityChecker;

    @Transactional
    public Visit createVisit(Visit visit) {
        if (visit.getDurationMinutes() == null) {
            visit.setDurationMinutes(AppConstants.DEFAULT_VISIT_DURATION_MINUTES);
        }
        validateWorkingHours(visit);
        validateNoConflict(visit, null);
        var saved = visitRepository.save(visit);
        eventPublisher.publishCreated(ENTITY_TYPE, saved.getId(), VisitSnapshot.from(saved));
        return saved;
    }

    public Visit getVisit(UUID id) {
        return visitRepository.findById(id).orElseThrow(() -> new VisitNotFoundException(id));
    }

    public List<Visit> getAllVisits() {
        return visitRepository.findAll();
    }

    @Transactional
    public Visit updateVisit(UUID id, Visit updated) {
        var existing = getVisit(id);
        var oldSnapshot = VisitSnapshot.from(existing);

        var changedFields =
                ChangeDetector.comparing(existing, updated)
                        .check("patientId", Visit::getPatientId)
                        .check("clientId", Visit::getClientId)
                        .check("veterinarianId", Visit::getVeterinarianId)
                        .check("visitDate", Visit::getVisitDate)
                        .check("durationMinutes", Visit::getDurationMinutes)
                        .check("status", Visit::getStatus)
                        .check("reason", Visit::getReason)
                        .check("interview", Visit::getInterview)
                        .check("examination", Visit::getExamination)
                        .check("diagnosis", Visit::getDiagnosis)
                        .check("treatment", Visit::getTreatment)
                        .check("recommendations", Visit::getRecommendations)
                        .check("notes", Visit::getNotes)
                        .check("weight", Visit::getWeight)
                        .check("temperature", Visit::getTemperature)
                        .check("nextVisitDate", Visit::getNextVisitDate)
                        .check("waitingRoomNotes", Visit::getWaitingRoomNotes)
                        .check("priority", Visit::getPriority)
                        .getChangedFields();

        validateScheduleChangeIfNeeded(existing, updated, id);
        applyVisitUpdates(existing, updated);

        var saved = visitRepository.save(existing);
        if (!changedFields.isEmpty()) {
            eventPublisher.publishUpdated(
                    ENTITY_TYPE, id, oldSnapshot, VisitSnapshot.from(saved), changedFields);
        }
        return saved;
    }

    private void validateScheduleChangeIfNeeded(Visit existing, Visit updated, UUID visitId) {
        var timeChanged =
                !Objects.equals(existing.getVisitDate(), updated.getVisitDate())
                        || !Objects.equals(
                                existing.getDurationMinutes(), updated.getDurationMinutes());
        var vetChanged = !Objects.equals(existing.getVeterinarianId(), updated.getVeterinarianId());

        if (timeChanged || vetChanged) {
            validateWorkingHours(updated);
            validateNoConflict(updated, visitId);
        }
    }

    private void applyVisitUpdates(Visit existing, Visit updated) {
        existing.setPatientId(updated.getPatientId());
        existing.setClientId(updated.getClientId());
        existing.setVeterinarianId(updated.getVeterinarianId());
        existing.setVeterinarianName(updated.getVeterinarianName());
        existing.setVisitDate(updated.getVisitDate());
        existing.setDurationMinutes(
                updated.getDurationMinutes() != null
                        ? updated.getDurationMinutes()
                        : AppConstants.DEFAULT_VISIT_DURATION_MINUTES);
        existing.setStatus(updated.getStatus());
        existing.setReason(updated.getReason());
        existing.setInterview(updated.getInterview());
        existing.setExamination(updated.getExamination());
        existing.setDiagnosis(updated.getDiagnosis());
        existing.setTreatment(updated.getTreatment());
        existing.setRecommendations(updated.getRecommendations());
        existing.setNotes(updated.getNotes());
        existing.setWeight(updated.getWeight());
        existing.setTemperature(updated.getTemperature());
        existing.setNextVisitDate(updated.getNextVisitDate());
        existing.setWaitingRoomNotes(updated.getWaitingRoomNotes());
        if (updated.getPriority() != null) {
            existing.setPriority(updated.getPriority());
        }

        // Update medications - clear and re-add
        existing.clearMedications();
        if (updated.getMedications() != null) {
            updated.getMedications().forEach(existing::addMedication);
        }
    }

    @Transactional
    public void deleteVisit(UUID id) {
        var visit = visitRepository.findById(id).orElseThrow(() -> new VisitNotFoundException(id));
        var snapshot = VisitSnapshot.from(visit);
        visitRepository.deleteById(id);
        eventPublisher.publishDeleted(ENTITY_TYPE, id, snapshot);
    }

    /** Get all visits for a specific patient */
    public List<Visit> getVisitsByPatient(UUID patientId) {
        return visitRepository.findByPatientIdOrderByVisitDateDesc(patientId);
    }

    /** Get all visits for a specific client */
    public List<Visit> getVisitsByClient(UUID clientId) {
        return visitRepository.findByClientId(clientId);
    }

    /** Get visits by status */
    public List<Visit> getVisitsByStatus(VisitStatus status) {
        return visitRepository.findByStatus(status);
    }

    /** Get visits for a specific date range */
    public List<Visit> getVisitsForDate(LocalDate date) {
        var range = DateTimeRange.forDay(date);
        return visitRepository.findByVisitDateBetween(range.start(), range.end());
    }

    /** Get visits for a veterinarian on a specific date */
    public List<Visit> getVisitsForVeterinarianOnDate(UUID veterinarianId, LocalDate date) {
        var range = DateTimeRange.forDay(date);
        return visitRepository.findByVeterinarianIdAndVisitDateBetween(
                veterinarianId, range.start(), range.end());
    }

    /** Get visits for a veterinarian in a date range (for weekly view) */
    public List<Visit> getVisitsForVeterinarian(
            UUID veterinarianId, LocalDate startDate, LocalDate endDate) {
        var range = DateTimeRange.forDateRange(startDate, endDate);
        return visitRepository.findByVeterinarianIdAndVisitDateBetween(
                veterinarianId, range.start(), range.end());
    }

    /** Get all visits for a veterinarian */
    public List<Visit> getVisitsByVeterinarian(UUID veterinarianId) {
        return visitRepository.findByVeterinarianId(veterinarianId);
    }

    /** Update visit status */
    @Transactional
    public Visit updateVisitStatus(UUID id, VisitStatus status) {
        var visit = getVisit(id);
        var oldStatus = visit.getStatus();
        if (Objects.equals(oldStatus, status)) {
            return visit; // No change
        }

        var oldSnapshot = VisitSnapshot.from(visit);
        visit.setStatus(status);
        var saved = visitRepository.save(visit);

        eventPublisher.publishUpdated(
                ENTITY_TYPE, id, oldSnapshot, VisitSnapshot.from(saved), Set.of("status"));
        return saved;
    }

    /** Reassign a visit to a different veterinarian and/or time (for drag-and-drop). */
    @Transactional
    public Visit reassignVisit(
            UUID id,
            UUID newVeterinarianId,
            String newVeterinarianName,
            LocalDateTime newDateTime) {
        var existing = getVisit(id);
        var oldSnapshot = VisitSnapshot.from(existing);

        var changedFields = new HashSet<String>();
        if (!Objects.equals(existing.getVeterinarianId(), newVeterinarianId)) {
            changedFields.add("veterinarianId");
        }
        if (!Objects.equals(existing.getVisitDate(), newDateTime)) {
            changedFields.add("visitDate");
        }

        // Create a temporary visit object for validation
        var tempVisit = new Visit();
        tempVisit.setVeterinarianId(newVeterinarianId);
        tempVisit.setVisitDate(newDateTime);
        tempVisit.setDurationMinutes(existing.getDurationMinutes());

        validateWorkingHours(tempVisit);
        validateNoConflict(tempVisit, id);

        existing.setVeterinarianId(newVeterinarianId);
        existing.setVeterinarianName(newVeterinarianName);
        existing.setVisitDate(newDateTime);

        var saved = visitRepository.save(existing);
        if (!changedFields.isEmpty()) {
            eventPublisher.publishUpdated(
                    ENTITY_TYPE, id, oldSnapshot, VisitSnapshot.from(saved), changedFields);
        }
        return saved;
    }

    /** Search visits by multiple criteria using a criteria object */
    public List<Visit> searchVisits(VisitSearchCriteria criteria) {
        Objects.requireNonNull(criteria, "Search criteria must not be null");
        if (!criteria.hasAnyCriteria()) {
            return visitRepository.findAll();
        }
        return visitRepository.search(
                criteria.patientId(),
                criteria.clientId(),
                criteria.status(),
                criteria.dateFrom(),
                criteria.dateTo());
    }

    /**
     * Search visits by multiple criteria.
     *
     * @deprecated Use {@link #searchVisits(VisitSearchCriteria)} instead
     */
    @Deprecated(forRemoval = true)
    public List<Visit> searchVisits(
            UUID patientId,
            UUID clientId,
            VisitStatus status,
            LocalDate dateFrom,
            LocalDate dateTo) {
        return searchVisits(new VisitSearchCriteria(patientId, clientId, status, dateFrom, dateTo));
    }

    // ===== WAITING ROOM OPERATIONS =====

    /**
     * Check in a patient to the waiting room. Transitions status from SCHEDULED to CHECKED_IN.
     *
     * @param visitId the visit ID
     * @param waitingRoomNotes optional notes for the waiting room
     * @param priority optional priority override (defaults to NORMAL)
     * @return the updated visit
     * @throws InvalidVisitStateException if the visit is not in SCHEDULED status
     */
    @Transactional
    public Visit checkInToWaitingRoom(
            UUID visitId, String waitingRoomNotes, VisitPriority priority) {
        var visit = getVisit(visitId);

        if (visit.getStatus() != VisitStatus.SCHEDULED) {
            throw new InvalidVisitStateException(
                    visit.getStatus(),
                    VisitStatus.CHECKED_IN,
                    "Only SCHEDULED visits can be checked in");
        }

        var oldSnapshot = VisitSnapshot.from(visit);
        var changedFields = new HashSet<String>();

        visit.setStatus(VisitStatus.CHECKED_IN);
        visit.setCheckedInAt(LocalDateTime.now());
        changedFields.add("status");
        changedFields.add("checkedInAt");

        if (waitingRoomNotes != null) {
            visit.setWaitingRoomNotes(waitingRoomNotes);
            changedFields.add("waitingRoomNotes");
        }

        if (priority != null) {
            visit.setPriority(priority);
            changedFields.add("priority");
        }

        var saved = visitRepository.save(visit);
        eventPublisher.publishUpdated(
                ENTITY_TYPE, visitId, oldSnapshot, VisitSnapshot.from(saved), changedFields);

        return saved;
    }

    /**
     * Get all visits currently in the waiting room for today. Results are ordered by priority
     * (URGENT first) then by check-in time.
     *
     * @return list of visits in the waiting room
     */
    public List<Visit> getWaitingRoomVisits() {
        var today = LocalDate.now();
        return visitRepository.findWaitingRoomVisits(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    /**
     * Start a visit from the waiting room. Transitions status from CHECKED_IN to IN_PROGRESS.
     *
     * @param visitId the visit ID
     * @return the updated visit
     * @throws InvalidVisitStateException if the visit is not in CHECKED_IN status
     */
    @Transactional
    public Visit startVisitFromWaitingRoom(UUID visitId) {
        var visit = getVisit(visitId);

        if (visit.getStatus() != VisitStatus.CHECKED_IN) {
            throw new InvalidVisitStateException(
                    visit.getStatus(),
                    VisitStatus.IN_PROGRESS,
                    "Only CHECKED_IN visits can be started from waiting room");
        }

        var oldSnapshot = VisitSnapshot.from(visit);
        visit.setStatus(VisitStatus.IN_PROGRESS);

        var saved = visitRepository.save(visit);
        eventPublisher.publishUpdated(
                ENTITY_TYPE, visitId, oldSnapshot, VisitSnapshot.from(saved), Set.of("status"));

        return saved;
    }

    /**
     * Mark a visit as no-show. Can transition from SCHEDULED or CHECKED_IN to NO_SHOW.
     *
     * @param visitId the visit ID
     * @return the updated visit
     * @throws InvalidVisitStateException if the visit cannot be marked as no-show
     */
    @Transactional
    public Visit markAsNoShow(UUID visitId) {
        var visit = getVisit(visitId);

        if (visit.getStatus() != VisitStatus.SCHEDULED
                && visit.getStatus() != VisitStatus.CHECKED_IN) {
            throw new InvalidVisitStateException(
                    visit.getStatus(),
                    VisitStatus.NO_SHOW,
                    "Only SCHEDULED or CHECKED_IN visits can be marked as NO_SHOW");
        }

        var oldSnapshot = VisitSnapshot.from(visit);
        visit.setStatus(VisitStatus.NO_SHOW);

        var saved = visitRepository.save(visit);
        eventPublisher.publishUpdated(
                ENTITY_TYPE, visitId, oldSnapshot, VisitSnapshot.from(saved), Set.of("status"));

        return saved;
    }

    /**
     * Update waiting room information for a checked-in visit.
     *
     * @param visitId the visit ID
     * @param waitingRoomNotes new notes
     * @param priority new priority
     * @return the updated visit
     * @throws InvalidVisitStateException if the visit is not in CHECKED_IN status
     */
    @Transactional
    public Visit updateWaitingRoomInfo(
            UUID visitId, String waitingRoomNotes, VisitPriority priority) {
        var visit = getVisit(visitId);

        if (visit.getStatus() != VisitStatus.CHECKED_IN) {
            throw new InvalidVisitStateException(
                    "Can only update waiting room info for CHECKED_IN visits. Current status: "
                            + visit.getStatus());
        }

        var oldSnapshot = VisitSnapshot.from(visit);
        var changedFields = new HashSet<String>();

        if (!Objects.equals(visit.getWaitingRoomNotes(), waitingRoomNotes)) {
            visit.setWaitingRoomNotes(waitingRoomNotes);
            changedFields.add("waitingRoomNotes");
        }

        if (priority != null && !Objects.equals(visit.getPriority(), priority)) {
            visit.setPriority(priority);
            changedFields.add("priority");
        }

        if (changedFields.isEmpty()) {
            return visit;
        }

        var saved = visitRepository.save(visit);
        eventPublisher.publishUpdated(
                ENTITY_TYPE, visitId, oldSnapshot, VisitSnapshot.from(saved), changedFields);

        return saved;
    }

    /** Validate that a visit does not conflict with existing appointments for the veterinarian. */
    private void validateNoConflict(Visit visit, UUID excludeVisitId) {
        if (visit.getVeterinarianId() == null) {
            return; // No veterinarian assigned, no conflict possible
        }

        var startTime = visit.getVisitDate();
        var duration =
                visit.getDurationMinutes() != null
                        ? visit.getDurationMinutes()
                        : AppConstants.DEFAULT_VISIT_DURATION_MINUTES;
        var endTime = startTime.plusMinutes(duration);

        var hasConflict =
                visitRepository.hasConflict(
                        visit.getVeterinarianId(), startTime, endTime, excludeVisitId);

        if (hasConflict) {
            throw new AppointmentConflictException(visit.getVeterinarianId(), startTime);
        }
    }

    /** Validate that a visit is scheduled within the veterinarian's working hours. */
    private void validateWorkingHours(Visit visit) {
        if (visit.getVeterinarianId() == null || visit.getVisitDate() == null) {
            return; // No veterinarian assigned or no date, skip validation
        }

        var startTime = visit.getVisitDate();
        var duration =
                visit.getDurationMinutes() != null
                        ? visit.getDurationMinutes()
                        : AppConstants.DEFAULT_VISIT_DURATION_MINUTES;
        // End time minus 1 minute to allow visits that end exactly at closing time
        var endTime = startTime.plusMinutes(duration - 1);

        // Check if it's a day off
        if (availabilityChecker.isDayOff(visit.getVeterinarianId(), startTime)) {
            throw OutsideWorkingHoursException.dayOff(visit.getVeterinarianId(), startTime);
        }

        // Check if start time is within working hours
        if (!availabilityChecker.isWorkingAt(visit.getVeterinarianId(), startTime)) {
            throw OutsideWorkingHoursException.outsideHours(visit.getVeterinarianId(), startTime);
        }

        // Check if end time is within working hours
        if (!availabilityChecker.isWorkingAt(visit.getVeterinarianId(), endTime)) {
            throw OutsideWorkingHoursException.outsideHours(visit.getVeterinarianId(), endTime);
        }
    }
}
