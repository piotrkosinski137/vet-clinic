package com.vetclinic.common.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * <p>This is a generic, reusable exception for any type of resource (Patient, Client, Appointment,
 * etc.). It automatically provides structured error information including the resource type and ID.
 *
 * <p>Usage examples:
 *
 * <pre>
 * // Simple usage with resource type and ID
 * throw new ResourceNotFoundException("Patient", patientId);
 *
 * // With UUID
 * throw new ResourceNotFoundException("Client", clientUuid);
 *
 * // With custom message
 * throw new ResourceNotFoundException("Appointment", appointmentId, "Appointment has been cancelled");
 *
 * // In service method
 * public Patient getPatient(UUID id) {
 *     return repository.findById(id)
 *         .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
 * }
 * </pre>
 *
 * @see BusinessException
 * @see ErrorCode#RESOURCE_NOT_FOUND
 */
public class ResourceNotFoundException extends BusinessException {

    /**
     * Constructs a ResourceNotFoundException with resource type and ID.
     *
     * @param resourceType the type of resource (e.g., "Patient", "Client")
     * @param resourceId the ID of the resource that was not found
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                String.format("%s with id '%s' was not found", resourceType, resourceId),
                Map.of("resourceType", resourceType, "resourceId", resourceId.toString()));
    }

    /**
     * Constructs a ResourceNotFoundException with custom message.
     *
     * @param resourceType the type of resource (e.g., "Patient", "Client")
     * @param resourceId the ID of the resource that was not found
     * @param customMessage additional context or custom message
     */
    public ResourceNotFoundException(String resourceType, Object resourceId, String customMessage) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                String.format(
                        "%s with id '%s' was not found: %s",
                        resourceType, resourceId, customMessage),
                Map.of(
                        "resourceType",
                        resourceType,
                        "resourceId",
                        resourceId.toString(),
                        "additionalInfo",
                        customMessage));
    }
}
