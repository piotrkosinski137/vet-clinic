package com.vetclinic.config;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vetclinic.common.tenant.TenantContext;

/**
 * Filter that extracts the clinic_id from the JWT token and sets it in TenantContext.
 *
 * <p>The clinic_id is expected to be a claim in the JWT token, configured in Keycloak as a user
 * attribute mapped to the token.
 *
 * <p>This filter runs after Spring Security authentication, so the JWT is already validated.
 */
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);
    private static final String CLINIC_ID_CLAIM = "clinic_id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            UUID clinicId = extractClinicIdFromToken();
            if (clinicId != null) {
                TenantContext.setCurrentClinicId(clinicId);
                log.debug("Set tenant context for clinic: {}", clinicId);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private UUID extractClinicIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String clinicIdStr = jwt.getClaimAsString(CLINIC_ID_CLAIM);

            if (clinicIdStr != null && !clinicIdStr.isBlank()) {
                try {
                    return UUID.fromString(clinicIdStr);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid clinic_id format in JWT: {}", clinicIdStr);
                }
            }
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip tenant filter for public endpoints
        return path.startsWith("/actuator")
                || path.startsWith("/api-docs")
                || path.startsWith("/swagger")
                || path.equals("/api/v1/auth/token");
    }
}
