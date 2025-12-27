package com.vetclinic.common.security;

import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.vetclinic.common.tenant.TenantContext;

/**
 * Helper to extract security context information from the current request. Used by event publishers
 * to capture user context for audit logging.
 */
@Component
public class SecurityContextHelper {

    private static final String UNKNOWN_USER = "system";
    private static final UUID SYSTEM_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Returns the current user ID from the JWT token. Falls back to system user ID if not
     * authenticated.
     */
    public UUID getCurrentUserId() {
        return extractFromJwt("sub").map(this::parseUuid).orElse(SYSTEM_USER_ID);
    }

    /**
     * Returns the current username from the JWT token. Falls back to "system" if not authenticated.
     */
    public String getCurrentUserName() {
        return extractFromJwt("preferred_username")
                .or(() -> extractFromJwt("name"))
                .or(() -> extractFromJwt("sub"))
                .orElse(UNKNOWN_USER);
    }

    /** Returns the current clinic ID from TenantContext. */
    public UUID getCurrentClinicId() {
        return TenantContext.getCurrentClinicId();
    }

    /** Returns the client IP address from the current request. */
    public Optional<String> getIpAddress() {
        return getCurrentRequest()
                .map(
                        request -> {
                            String xForwardedFor = request.getHeader("X-Forwarded-For");
                            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                                return xForwardedFor.split(",")[0].trim();
                            }
                            return request.getRemoteAddr();
                        });
    }

    /** Returns the User-Agent header from the current request. */
    public Optional<String> getUserAgent() {
        return getCurrentRequest().map(request -> request.getHeader("User-Agent"));
    }

    private Optional<String> extractFromJwt(String claim) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Object value = jwt.getClaim(claim);
            return Optional.ofNullable(value).map(Object::toString);
        }
        return Optional.empty();
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return SYSTEM_USER_ID;
        }
    }

    private Optional<HttpServletRequest> getCurrentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Optional.ofNullable(attrs).map(ServletRequestAttributes::getRequest);
    }
}
