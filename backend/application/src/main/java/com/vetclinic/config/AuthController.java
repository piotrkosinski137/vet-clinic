package com.vetclinic.config;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Auth controller for testing purposes. Provides endpoint to get token from Keycloak without using
 * the UI.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KeycloakProperties keycloakProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get access token from Keycloak using password grant. This is for API testing only - not for
     * production use.
     *
     * <p>Example request: POST /auth/token {"username": "user", "password": "user"}
     *
     * <p>Response includes access_token that can be used as Bearer token.
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> getToken(@RequestBody TokenRequest request) {
        String tokenUrl =
                String.format(
                        "%s/realms/%s/protocol/openid-connect/token",
                        keycloakProperties.getAuthServerUrl(), keycloakProperties.getRealm());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", keycloakProperties.getClientId());
        body.add("client_secret", keycloakProperties.getClientSecret());
        body.add("username", request.username());
        body.add("password", request.password());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(tokenUrl, entity, Map.class);
            return ResponseEntity.ok(response);
        } catch (RestClientException e) {
            log.error("Failed to get token from Keycloak: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "authentication_failed", "message", e.getMessage()));
        }
    }

    public record TokenRequest(String username, String password) {}
}
