package com.vetclinic.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class ApiErrorTest {

    @Test
    void shouldBuildApiError() {
        Instant now = Instant.now();

        ApiError error =
                ApiError.builder()
                        .type("https://api.vetclinic.com/errors/validation")
                        .title("Validation Error")
                        .status(400)
                        .detail("One or more fields failed validation")
                        .instance("/api/v1/patients")
                        .timestamp(now)
                        .errors(
                                List.of(
                                        ApiError.FieldError.builder()
                                                .field("name")
                                                .message("must not be blank")
                                                .rejectedValue("")
                                                .build()))
                        .build();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getTitle()).isEqualTo("Validation Error");
        assertThat(error.getErrors()).hasSize(1);
        assertThat(error.getErrors().get(0).getField()).isEqualTo("name");
    }

    @Test
    void shouldBuildApiErrorWithoutFieldErrors() {
        ApiError error =
                ApiError.builder()
                        .type("https://api.vetclinic.com/errors/not-found")
                        .title("Not Found")
                        .status(404)
                        .detail("Patient with ID 123 not found")
                        .build();

        assertThat(error.getStatus()).isEqualTo(404);
        assertThat(error.getErrors()).isEmpty();
    }
}
