package com.vetclinic.visit.api.dto;

import jakarta.validation.constraints.NotNull;

import com.vetclinic.visit.domain.model.VisitStatus;

/** Request DTO for updating visit status. */
public record VisitStatusUpdateRequest(
        @NotNull(message = "Status is required") VisitStatus status) {}
