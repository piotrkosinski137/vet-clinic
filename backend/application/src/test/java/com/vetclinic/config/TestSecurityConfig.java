package com.vetclinic.config;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import com.vetclinic.common.tenant.TenantContext;

/**
 * Test security configuration that disables OAuth2/Keycloak authentication. This allows integration
 * tests to focus on business logic without requiring a running Keycloak instance.
 */
@TestConfiguration
public class TestSecurityConfig {

    /** Default clinic ID used in tests - matches the one created by V2__add_multitenancy.sql */
    public static final UUID TEST_CLINIC_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Bean
    @Primary
    @Order(Ordered.HIGHEST_PRECEDENCE)
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

    /**
     * Filter that sets the TenantContext for tests. Since security is disabled in tests, the
     * regular TenantFilter won't find a JWT token. This filter ensures the test clinic ID is always
     * set.
     */
    @Bean
    @Order(0) // Run before other filters
    public Filter testTenantFilter() {
        return new Filter() {
            @Override
            public void doFilter(
                    ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                try {
                    TenantContext.setCurrentClinicId(TEST_CLINIC_ID);
                    chain.doFilter(request, response);
                } finally {
                    TenantContext.clear();
                }
            }
        };
    }
}
