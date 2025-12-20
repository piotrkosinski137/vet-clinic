package com.vetclinic.health;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator that checks if Keycloak server is reachable. This is designed to be
 * graceful - if Keycloak is not running, it will show as DOWN but won't crash the application.
 */
@Component
public class KeycloakHealthIndicator implements HealthIndicator {

    private final String keycloakAuthServerUrl;
    private final String keycloakRealm;

    public KeycloakHealthIndicator(
            @Value("${keycloak.auth-server-url}") String keycloakAuthServerUrl,
            @Value("${keycloak.realm}") String keycloakRealm) {
        this.keycloakAuthServerUrl = keycloakAuthServerUrl;
        this.keycloakRealm = keycloakRealm;
    }

    @Override
    public Health health() {
        try {
            // Check Keycloak realm endpoint
            String realmUrl = keycloakAuthServerUrl + "/realms/" + keycloakRealm;
            URL url = URI.create(realmUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000); // 3 second timeout
            connection.setReadTimeout(3000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            if (responseCode == 200) {
                return Health.up()
                        .withDetail("keycloakUrl", keycloakAuthServerUrl)
                        .withDetail("realm", keycloakRealm)
                        .withDetail("realmEndpoint", realmUrl)
                        .withDetail("responseCode", responseCode)
                        .build();
            } else {
                return Health.down()
                        .withDetail("keycloakUrl", keycloakAuthServerUrl)
                        .withDetail("realm", keycloakRealm)
                        .withDetail("realmEndpoint", realmUrl)
                        .withDetail("responseCode", responseCode)
                        .withDetail("reason", "Unexpected response code from Keycloak")
                        .build();
            }
        } catch (Exception e) {
            // Graceful degradation - Keycloak might not be running in development
            return Health.down()
                    .withDetail("keycloakUrl", keycloakAuthServerUrl)
                    .withDetail("realm", keycloakRealm)
                    .withDetail("error", e.getClass().getSimpleName())
                    .withDetail("message", e.getMessage())
                    .withDetail(
                            "note",
                            "Keycloak is optional in development - application can still run")
                    .build();
        }
    }
}
