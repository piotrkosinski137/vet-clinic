package com.vetclinic.common.api;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/** Standard API error response following RFC 7807 Problem Details. */
public record ApiError(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        Instant timestamp,
        List<FieldError> errors) {

    /** Compact constructor to ensure immutability. */
    public ApiError {
        errors = errors == null ? Collections.emptyList() : List.copyOf(errors);
    }

    public record FieldError(String field, String message, Object rejectedValue) {}
}
