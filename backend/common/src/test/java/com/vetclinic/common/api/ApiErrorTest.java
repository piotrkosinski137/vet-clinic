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
                new ApiError(
                        "https://api.vetclinic.com/errors/validation",
                        "Validation Error",
                        400,
                        "One or more fields failed validation",
                        "/api/v1/patients",
                        now,
                        List.of(new ApiError.FieldError("name", "must not be blank", "")));

        assertThat(error.status()).isEqualTo(400);
        assertThat(error.title()).isEqualTo("Validation Error");
        assertThat(error.errors()).hasSize(1);
        assertThat(error.errors().get(0).field()).isEqualTo("name");
    }

    @Test
    void shouldBuildApiErrorWithoutFieldErrors() {
        ApiError error =
                new ApiError(
                        "https://api.vetclinic.com/errors/not-found",
                        "Not Found",
                        404,
                        "Patient with ID 123 not found",
                        null,
                        null,
                        null);

        assertThat(error.status()).isEqualTo(404);
        assertThat(error.errors()).isEmpty();
    }
}
