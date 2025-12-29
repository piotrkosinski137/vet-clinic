package com.vetclinic.api;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.billing.domain.ClientDebtService;
import com.vetclinic.client.domain.ClientService;
import com.vetclinic.client.domain.model.Client;
import com.vetclinic.common.exception.ResourceNotFoundException;
import com.vetclinic.common.tenant.TenantAccessDeniedException;
import com.vetclinic.patient.domain.PatientService;
import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.visit.api.dto.WaitingRoomVisitResponse;
import com.vetclinic.visit.domain.VisitService;
import com.vetclinic.visit.domain.model.Visit;
import com.vetclinic.visit.domain.model.VisitPriority;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Facade service that coordinates between multiple modules to provide enriched waiting room data.
 * This includes patient details, client info, and financial data (client debt).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WaitingRoomFacadeService {

    private final VisitService visitService;
    private final PatientService patientService;
    private final ClientService clientService;
    private final ClientDebtService clientDebtService;

    /**
     * Get all visits in the waiting room with enriched patient, client, and financial data.
     *
     * @return list of enriched waiting room visit responses
     */
    public List<WaitingRoomVisitResponse> getEnrichedWaitingRoomVisits() {
        var visits = visitService.getWaitingRoomVisits();
        return visits.stream().map(this::enrichVisit).toList();
    }

    private WaitingRoomVisitResponse enrichVisit(Visit visit) {
        var patient = fetchPatientSafely(visit.getPatientId());
        var client = fetchClientSafely(visit.getClientId());
        var clientDebt = calculateClientDebtSafely(visit.getClientId());

        var waitingTimeMinutes = calculateWaitingTime(visit.getCheckedInAt());

        return new WaitingRoomVisitResponse(
                visit.getId(),
                visit.getVisitDate(),
                visit.getVisitType(),
                visit.getReason(),
                visit.getCheckedInAt(),
                waitingTimeMinutes,
                visit.getWaitingRoomNotes(),
                visit.getPriority() != null ? visit.getPriority() : VisitPriority.NORMAL,
                visit.getPatientId(),
                patient != null ? patient.getName() : null,
                patient != null && patient.getSpecies() != null
                        ? patient.getSpecies().name()
                        : null,
                patient != null ? patient.getBreed() : null,
                patient != null && patient.getLabels() != null
                        ? patient.getLabels().stream().map(Enum::name).toList()
                        : List.of(),
                visit.getClientId(),
                client != null ? client.getFirstName() + " " + client.getLastName() : null,
                client != null ? client.getPhone() : null,
                visit.getVeterinarianId(),
                visit.getVeterinarianName(),
                null, // estimatedCost - to be implemented based on visit type / price list
                clientDebt,
                clientDebt // totalToPay - for now just the debt, can be extended
                );
    }

    private Patient fetchPatientSafely(UUID patientId) {
        if (patientId == null) {
            return null;
        }
        try {
            return patientService.getPatient(patientId);
        } catch (ResourceNotFoundException e) {
            log.warn("Patient not found {}: {}", patientId, e.getMessage());
            return null;
        } catch (TenantAccessDeniedException e) {
            log.warn("Access denied to patient {}: {}", patientId, e.getMessage());
            return null;
        }
    }

    private Client fetchClientSafely(UUID clientId) {
        if (clientId == null) {
            return null;
        }
        try {
            return clientService.getClient(clientId);
        } catch (ResourceNotFoundException e) {
            log.warn("Client not found {}: {}", clientId, e.getMessage());
            return null;
        } catch (TenantAccessDeniedException e) {
            log.warn("Access denied to client {}: {}", clientId, e.getMessage());
            return null;
        }
    }

    private BigDecimal calculateClientDebtSafely(UUID clientId) {
        if (clientId == null) {
            return BigDecimal.ZERO;
        }
        try {
            var debtSummary = clientDebtService.calculateClientDebt(clientId);
            return debtSummary.totalOutstanding();
        } catch (ResourceNotFoundException e) {
            log.warn("Client not found for debt calculation {}: {}", clientId, e.getMessage());
            return BigDecimal.ZERO;
        } catch (TenantAccessDeniedException e) {
            log.warn("Access denied to client debt {}: {}", clientId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private long calculateWaitingTime(LocalDateTime checkedInAt) {
        if (checkedInAt == null) {
            return 0;
        }
        return Duration.between(checkedInAt, LocalDateTime.now()).toMinutes();
    }
}
