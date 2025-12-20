package com.vetclinic.common.api;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Value;

/**
 * Standard API error response following RFC 7807 Problem Details.
 */
@Value
@Builder
public class ApiError {

    String type;
    String title;
    int status;
    String detail;
    String instance;
    Instant timestamp;
    List<FieldError> errors;

    @Value
    @Builder
    public static class FieldError {
        String field;
        String message;
        Object rejectedValue;
    }
}
