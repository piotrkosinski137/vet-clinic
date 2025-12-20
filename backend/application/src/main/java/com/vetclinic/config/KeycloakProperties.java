package com.vetclinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration properties for Keycloak integration. Allows externalization of Keycloak settings
 * via application.yml or environment variables.
 *
 * <p>Environment variables: - KEYCLOAK_URL: Base URL of Keycloak server - KEYCLOAK_REALM: Keycloak
 * realm name - KEYCLOAK_CLIENT_ID: OAuth2/OIDC client ID - KEYCLOAK_CLIENT_SECRET: OAuth2/OIDC
 * client secret
 */
@Data
@Component
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    /**
     * Base URL of the Keycloak authentication server. Example: "http://localhost:8180" or
     * "https://auth.example.com" Environment variable: KEYCLOAK_URL
     */
    private String authServerUrl = "http://localhost:8180";

    /** Keycloak realm name. Environment variable: KEYCLOAK_REALM */
    private String realm = "vetclinic";

    /** OAuth2/OIDC client ID configured in Keycloak. Environment variable: KEYCLOAK_CLIENT_ID */
    private String clientId = "vetclinic-app";

    /**
     * OAuth2/OIDC client secret configured in Keycloak. WARNING: This should be stored securely,
     * preferably via environment variables in production. Environment variable:
     * KEYCLOAK_CLIENT_SECRET
     */
    private String clientSecret = "vetclinic-secret";
}
