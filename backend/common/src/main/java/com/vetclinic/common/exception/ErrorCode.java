package com.vetclinic.common.exception;

/**
 * Standard error codes for business exceptions.
 *
 * <p>This enum defines all possible error codes that can occur in the application. Each code should
 * be unique and descriptive.
 *
 * <p>Usage example:
 *
 * <pre>
 * throw new ResourceNotFoundException(
 *     ErrorCode.RESOURCE_NOT_FOUND,
 *     "Patient with id " + id + " was not found"
 * );
 * </pre>
 */
public enum ErrorCode {
    /** Generic resource not found error */
    RESOURCE_NOT_FOUND,

    /** Resource already exists (e.g., duplicate email) */
    RESOURCE_ALREADY_EXISTS,

    /** Invalid operation or state */
    INVALID_OPERATION,

    /** Validation failed */
    VALIDATION_FAILED,

    /** Unauthorized access */
    UNAUTHORIZED,

    /** Access forbidden */
    FORBIDDEN,

    /** Insufficient stock for operation */
    INSUFFICIENT_STOCK;

    /**
     * Returns the error code in lowercase with underscores replaced by hyphens.
     *
     * <p>Example: RESOURCE_NOT_FOUND -> resource-not-found
     */
    public String toKebabCase() {
        return name().toLowerCase().replace('_', '-');
    }
}
