package com.vetclinic.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import com.vetclinic.common.constants.AppConstants;
import com.vetclinic.common.constants.ErrorMessages;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing users in Keycloak. Provides functionality to create users with email
 * verification and assign them to clinics.
 */
@Slf4j
@Service
public class KeycloakAdminService {

    private final KeycloakProperties keycloakProperties;
    private Keycloak keycloakAdmin;

    public KeycloakAdminService(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }

    @PostConstruct
    void initKeycloakAdmin() {
        this.keycloakAdmin =
                KeycloakBuilder.builder()
                        .serverUrl(keycloakProperties.getAuthServerUrl())
                        .realm("master")
                        .username(keycloakProperties.getAdminUsername())
                        .password(keycloakProperties.getAdminPassword())
                        .clientId("admin-cli")
                        .build();
        log.info(
                "Keycloak admin client initialized for server: {}",
                keycloakProperties.getAuthServerUrl());
    }

    private Keycloak getKeycloakAdmin() {
        return keycloakAdmin;
    }

    private RealmResource getRealmResource() {
        return getKeycloakAdmin().realm(keycloakProperties.getRealm());
    }

    private UsersResource getUsersResource() {
        return getRealmResource().users();
    }

    /**
     * Creates a new user in Keycloak and sends a password reset email.
     *
     * @param email User's email address
     * @param firstName User's first name
     * @param lastName User's last name
     * @param clinicId The clinic ID to assign to this user
     * @return The created user's Keycloak ID
     * @throws KeycloakUserCreationException if user creation fails
     */
    public String createUserWithEmailInvitation(
            String email, String firstName, String lastName, UUID clinicId) {
        log.info("Creating Keycloak user for email: {}", email);

        validateUserDoesNotExist(email);

        // Create user representation
        UserRepresentation user = buildUserRepresentation(email, firstName, lastName, clinicId);
        user.setEmailVerified(false);

        // Create the user and get ID
        String userId = createUserAndGetId(user);

        log.info("User created with ID: {}", userId);

        // Send password reset email (which serves as invitation)
        try {
            getUsersResource().get(userId).executeActionsEmail(List.of("UPDATE_PASSWORD"));
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.warn(
                    "Failed to send password reset email to {}. User may need manual password"
                            + " reset.",
                    email,
                    e);
        }

        return userId;
    }

    /**
     * Creates a user with a temporary password (for development/testing).
     *
     * @param email User's email address
     * @param firstName User's first name
     * @param lastName User's last name
     * @param clinicId The clinic ID to assign to this user
     * @param temporaryPassword Temporary password (user must change on first login)
     * @return The created user's Keycloak ID
     */
    public String createUserWithTemporaryPassword(
            String email,
            String firstName,
            String lastName,
            UUID clinicId,
            String temporaryPassword) {
        log.info("Creating Keycloak user with temporary password for email: {}", email);

        validateUserDoesNotExist(email);

        // Create user representation
        UserRepresentation user = buildUserRepresentation(email, firstName, lastName, clinicId);
        user.setEmailVerified(true); // Skip email verification for temp password flow

        // Set temporary password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(temporaryPassword);
        credential.setTemporary(true);
        user.setCredentials(Collections.singletonList(credential));

        // Create the user and get ID
        String userId = createUserAndGetId(user);

        log.info("User created with temporary password. ID: {}", userId);
        return userId;
    }

    /**
     * Gets the clinic ID for a user by their Keycloak user ID.
     *
     * @param userId Keycloak user ID
     * @return The clinic ID or null if not set
     */
    public UUID getClinicIdForUser(String userId) {
        try {
            UserRepresentation user = getUsersResource().get(userId).toRepresentation();
            Map<String, List<String>> attributes = user.getAttributes();
            if (attributes != null
                    && attributes.containsKey(AppConstants.KEYCLOAK_CLINIC_ID_ATTRIBUTE)) {
                List<String> clinicIds = attributes.get(AppConstants.KEYCLOAK_CLINIC_ID_ATTRIBUTE);
                if (!clinicIds.isEmpty()) {
                    return UUID.fromString(clinicIds.get(0));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get clinic ID for user: {}", userId, e);
        }
        return null;
    }

    // ============================================
    // Private Helper Methods
    // ============================================

    private void validateUserDoesNotExist(String email) {
        List<UserRepresentation> existingUsers = getUsersResource().searchByEmail(email, true);
        if (!existingUsers.isEmpty()) {
            throw new KeycloakUserCreationException(
                    String.format(ErrorMessages.KEYCLOAK_USER_EXISTS, email));
        }
    }

    private UserRepresentation buildUserRepresentation(
            String email, String firstName, String lastName, UUID clinicId) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setEmail(email);
        user.setUsername(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Set clinic_id as a user attribute
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(
                AppConstants.KEYCLOAK_CLINIC_ID_ATTRIBUTE,
                Collections.singletonList(clinicId.toString()));
        user.setAttributes(attributes);

        return user;
    }

    private String createUserAndGetId(UserRepresentation user) {
        Response response = getUsersResource().create(user);
        int status = response.getStatus();
        String locationHeader = response.getHeaderString("Location");
        response.close();

        if (status != 201) {
            log.error("Failed to create user. Status: {}", status);
            throw new KeycloakUserCreationException(
                    String.format(ErrorMessages.KEYCLOAK_USER_CREATION_FAILED, status));
        }

        if (locationHeader == null) {
            throw new KeycloakUserCreationException(ErrorMessages.KEYCLOAK_LOCATION_HEADER_MISSING);
        }

        return locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
    }

    /** Exception thrown when Keycloak user creation fails. */
    public static class KeycloakUserCreationException extends RuntimeException {
        public KeycloakUserCreationException(String message) {
            super(message);
        }

        public KeycloakUserCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
