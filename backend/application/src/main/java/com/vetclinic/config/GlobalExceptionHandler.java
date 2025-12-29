package com.vetclinic.config;

import java.time.Instant;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.vetclinic.client.domain.ClientNotFoundException;
import com.vetclinic.client.domain.EmailAlreadyExistsException;
import com.vetclinic.common.api.ApiError;
import com.vetclinic.common.exception.BusinessException;
import com.vetclinic.common.tenant.TenantAccessDeniedException;
import com.vetclinic.config.KeycloakAdminService.KeycloakUserCreationException;
import com.vetclinic.patient.domain.PatientNotFoundException;
import com.vetclinic.veterinarian.domain.DayOffConflictException;
import com.vetclinic.veterinarian.domain.VeterinarianEmailAlreadyExistsException;
import com.vetclinic.veterinarian.domain.VeterinarianNotFoundException;
import com.vetclinic.visit.domain.AppointmentConflictException;
import com.vetclinic.visit.domain.InvalidVisitStateException;
import com.vetclinic.visit.domain.OutsideWorkingHoursException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final MediaType PROBLEM_JSON = MediaType.valueOf("application/problem+json");

    /**
     * Handles all BusinessException subclasses with their error code and HTTP status.
     *
     * <p>This handler automatically extracts error code, HTTP status, and context data from the
     * exception. This is the generic handler that works with all custom business exceptions.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        log.debug("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ApiError error =
                new ApiError(
                        "https://api.vetclinic.com/errors/" + ex.getErrorCode().toKebabCase(),
                        ex.getErrorCode().name().replace('_', ' '),
                        ex.getHttpStatus().value(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        Instant.now(),
                        null);

        return ResponseEntity.status(ex.getHttpStatus()).contentType(PROBLEM_JSON).body(error);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ApiError> handlePatientNotFound(
            PatientNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Patient Not Found",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ApiError> handleClientNotFound(
            ClientNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND, "Client Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(VeterinarianNotFoundException.class)
    public ResponseEntity<ApiError> handleVeterinarianNotFound(
            VeterinarianNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Veterinarian Not Found",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(DayOffConflictException.class)
    public ResponseEntity<ApiError> handleDayOffConflict(
            DayOffConflictException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT, "Day Off Conflict", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailExists(
            EmailAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Email Already Exists",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<ApiError> handleAppointmentConflict(
            AppointmentConflictException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Appointment Conflict",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(OutsideWorkingHoursException.class)
    public ResponseEntity<ApiError> handleOutsideWorkingHours(
            OutsideWorkingHoursException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Outside Working Hours",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(InvalidVisitStateException.class)
    public ResponseEntity<ApiError> handleInvalidVisitState(
            InvalidVisitStateException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Invalid Visit State",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(KeycloakUserCreationException.class)
    public ResponseEntity<ApiError> handleKeycloakUserCreation(
            KeycloakUserCreationException ex, HttpServletRequest request) {
        log.warn("Keycloak user creation failed: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "User Creation Failed",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(VeterinarianEmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleVeterinarianEmailExists(
            VeterinarianEmailAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Veterinarian Email Already Exists",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(TenantAccessDeniedException.class)
    public ResponseEntity<ApiError> handleTenantAccessDenied(
            TenantAccessDeniedException ex, HttpServletRequest request) {
        log.warn(
                "Tenant access denied: {} attempted to access {} in clinic {}",
                ex.getUserClinicId(),
                ex.getResourceType(),
                ex.getRequestedClinicId());
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                "You do not have permission to access this resource",
                request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                "You do not have the required role to perform this action",
                request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldError> fieldErrors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(
                                error ->
                                        new ApiError.FieldError(
                                                error.getField(),
                                                error.getDefaultMessage(),
                                                error.getRejectedValue()))
                        .toList();

        ApiError error =
                new ApiError(
                        "https://api.vetclinic.com/errors/validation",
                        "Validation Error",
                        HttpStatus.BAD_REQUEST.value(),
                        "One or more fields failed validation",
                        request.getRequestURI(),
                        Instant.now(),
                        fieldErrors);

        return ResponseEntity.badRequest().contentType(PROBLEM_JSON).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Request",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Data Conflict",
                "The operation violates data integrity constraints",
                request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed Request",
                "Request body is missing or malformed",
                request.getRequestURI());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported Media Type",
                "Content-Type '" + ex.getContentType() + "' is not supported",
                request.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method Not Allowed",
                "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
                request.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Missing Parameter",
                "Required parameter '" + ex.getParameterName() + "' is missing",
                request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Parameter",
                "Parameter '" + ex.getName() + "' has invalid value",
                request.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                "The requested resource was not found",
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI());
    }

    private ResponseEntity<ApiError> buildErrorResponse(
            HttpStatus status, String title, String detail, String instance) {
        ApiError error =
                new ApiError(
                        "https://api.vetclinic.com/errors/" + status.name().toLowerCase(),
                        title,
                        status.value(),
                        detail,
                        instance,
                        Instant.now(),
                        null);

        return ResponseEntity.status(status).contentType(PROBLEM_JSON).body(error);
    }
}
