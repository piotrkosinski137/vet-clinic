package com.vetclinic.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration that disables OAuth2/Keycloak authentication. This allows integration
 * tests to focus on business logic without requiring a running Keycloak instance.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2ResourceServer(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        // Return a mock JwtDecoder that does nothing - we don't need JWT validation in tests
        return token ->
                Jwt.withTokenValue(token).header("alg", "none").claim("sub", "test-user").build();
    }
}
