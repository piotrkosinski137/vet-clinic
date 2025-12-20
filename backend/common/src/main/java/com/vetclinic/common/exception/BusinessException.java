package com.vetclinic.common.exception;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * Base class for all business exceptions in the application.
 *
 * <p>This exception provides a structured way to handle business logic errors with:
 *
 * <ul>
 *   <li>Error codes - standardized error identifiers
 *   <li>HTTP status - appropriate status code for REST APIs
 *   <li>Context data - additional information about the error
 * </ul>
 *
 * <p>Usage example:
 *
 * <pre>
 * public class ResourceNotFoundException extends BusinessException {
 *     public ResourceNotFoundException(String resourceType, Object resourceId) {
 *         super(
 *             ErrorCode.RESOURCE_NOT_FOUND,
 *             HttpStatus.NOT_FOUND,
 *             String.format("%s with id '%s' was not found", resourceType, resourceId),
 *             Map.of("resourceType", resourceType, "resourceId", resourceId)
 *         );
 *     }
 * }
 * </pre>
 *
 * @see ErrorCode
 * @see ResourceNotFoundException
 */
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final Map<String, Object> context;

    /**
     * Constructs a new business exception with full context.
     *
     * @param errorCode the error code identifying the error type
     * @param httpStatus the HTTP status code to return
     * @param message the error message
     * @param context additional context data (can be null)
     */
    protected BusinessException(
            ErrorCode errorCode,
            HttpStatus httpStatus,
            String message,
            Map<String, Object> context) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.context = context == null ? Collections.emptyMap() : Map.copyOf(context);
    }

    /**
     * Constructs a new business exception without context data.
     *
     * @param errorCode the error code identifying the error type
     * @param httpStatus the HTTP status code to return
     * @param message the error message
     */
    protected BusinessException(ErrorCode errorCode, HttpStatus httpStatus, String message) {
        this(errorCode, httpStatus, message, null);
    }

    /**
     * Constructs a new business exception with cause.
     *
     * @param errorCode the error code identifying the error type
     * @param httpStatus the HTTP status code to return
     * @param message the error message
     * @param context additional context data (can be null)
     * @param cause the cause of this exception
     */
    protected BusinessException(
            ErrorCode errorCode,
            HttpStatus httpStatus,
            String message,
            Map<String, Object> context,
            Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.context = context == null ? Collections.emptyMap() : Map.copyOf(context);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
