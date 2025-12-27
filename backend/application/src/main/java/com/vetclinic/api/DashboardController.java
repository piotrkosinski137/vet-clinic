package com.vetclinic.api;

import static com.vetclinic.common.security.Roles.HAS_ANY_ROLE;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.api.dto.DashboardStatsResponse;
import com.vetclinic.billing.domain.BillingService;
import com.vetclinic.billing.domain.InventoryService;
import com.vetclinic.billing.domain.model.InvoiceStatus;
import com.vetclinic.client.domain.ClientService;
import com.vetclinic.patient.domain.PatientService;
import com.vetclinic.visit.domain.VisitService;
import com.vetclinic.visit.domain.model.VisitStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard statistics API")
public class DashboardController {

    private final PatientService patientService;
    private final ClientService clientService;
    private final VisitService visitService;
    private final BillingService billingService;
    private final InventoryService inventoryService;
    private final Clock clock;

    @GetMapping("/stats")
    @PreAuthorize(HAS_ANY_ROLE)
    @Operation(
            summary = "Get dashboard statistics",
            description = "Returns key metrics for the dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        LocalDate today = LocalDate.now(clock);

        // Get counts efficiently using count queries
        long totalPatients = patientService.countPatients();
        long totalClients = clientService.countClients();

        // Visits for today
        var visitsToday = visitService.getVisitsForDate(today);
        long visitsCount = visitsToday.size();
        long completedToday =
                visitsToday.stream().filter(v -> v.getStatus() == VisitStatus.COMPLETED).count();
        long scheduledToday =
                visitsToday.stream().filter(v -> v.getStatus() == VisitStatus.SCHEDULED).count();

        // Pending (issued but unpaid) invoices
        long pendingInvoices = billingService.getInvoicesByStatus(InvoiceStatus.ISSUED).size();

        // Low stock items
        long lowStockItems = inventoryService.getInventoryItems(true).size();

        DashboardStatsResponse stats =
                DashboardStatsResponse.builder()
                        .totalPatients(totalPatients)
                        .totalClients(totalClients)
                        .visitsToday(visitsCount)
                        .completedVisitsToday(completedToday)
                        .scheduledVisitsToday(scheduledToday)
                        .pendingInvoices(pendingInvoices)
                        .lowStockItems(lowStockItems)
                        .build();

        return ResponseEntity.ok(stats);
    }
}
