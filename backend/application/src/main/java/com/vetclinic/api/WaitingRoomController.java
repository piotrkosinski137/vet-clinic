package com.vetclinic.api;

import static com.vetclinic.common.security.Roles.HAS_ANY_ROLE;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.visit.api.dto.WaitingRoomVisitResponse;

import lombok.RequiredArgsConstructor;

/**
 * Controller for enriched waiting room operations. This controller uses the facade service to
 * combine data from multiple modules (visit, patient, client, billing) into a single response.
 */
@RestController
@RequestMapping("/api/v1/waiting-room")
@RequiredArgsConstructor
public class WaitingRoomController {

    private final WaitingRoomFacadeService waitingRoomFacadeService;

    /**
     * Get all visits in the waiting room with enriched patient, client, and financial data. This
     * endpoint is optimized for the waiting room display, combining data from multiple services.
     *
     * @return list of enriched waiting room visit responses
     */
    @GetMapping
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<WaitingRoomVisitResponse>> getEnrichedWaitingRoom() {
        var visits = waitingRoomFacadeService.getEnrichedWaitingRoomVisits();
        return ResponseEntity.ok(visits);
    }
}
