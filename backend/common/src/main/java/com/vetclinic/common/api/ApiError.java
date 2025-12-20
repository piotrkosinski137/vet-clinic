package com.vetclinic.common.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

/** Standard API error response following RFC 7807 Problem Details. */
@Getter
@Builder
public class ApiError {

    private final String type;
    private final String title;
    private final int status;
    private final String detail;
    private final String instance;
    private final Instant timestamp;
    private final List<FieldError> errors;

    /** Returns an unmodifiable view of the errors list. */
    public List<FieldError> getErrors() {
        return errors == null ? Collections.emptyList() : Collections.unmodifiableList(errors);
    }

    /** Custom builder to ensure immutability. */
    public static class ApiErrorBuilder {
        /** Sets errors with defensive copy. */
        public ApiErrorBuilder errors(List<FieldError> errors) {
            this.errors = errors == null ? null : new ArrayList<>(errors);
            return this;
        }
    }

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
    }
}
