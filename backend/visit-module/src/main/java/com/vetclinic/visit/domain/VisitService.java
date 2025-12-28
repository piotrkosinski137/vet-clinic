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

    /** Search visits by multiple criteria */
    public List<Visit> searchVisits(
            UUID patientId,
            UUID clientId,
            VisitStatus status,
            LocalDate dateFrom,
            LocalDate dateTo) {
        return visitRepository.search(patientId, clientId, status, dateFrom, dateTo);
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

        var dateTime = visit.getVisitDate();

        // Check if it's a day off
        if (availabilityChecker.isDayOff(visit.getVeterinarianId(), dateTime)) {
            throw OutsideWorkingHoursException.dayOff(visit.getVeterinarianId(), dateTime);
        }

        // Check if it's within working hours
        if (!availabilityChecker.isWorkingAt(visit.getVeterinarianId(), dateTime)) {
            throw OutsideWorkingHoursException.outsideHours(visit.getVeterinarianId(), dateTime);
        }
    }
}
