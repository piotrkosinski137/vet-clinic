package com.vetclinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration properties for CORS settings. Allows externalization of CORS configuration via
 * application.yml or environment variables.
 */
@Data
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * Comma-separated list of allowed origins. Example:
     * "http://localhost:3000,http://localhost:3001" Environment variable: CORS_ALLOWED_ORIGINS
     */
    private String allowedOrigins = "http://localhost:3000";

    /** Comma-separated list of allowed HTTP methods. Default: GET,POST,PUT,DELETE,OPTIONS */
    private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";

    /** Comma-separated list of allowed headers. Default: "*" (all headers) */
    private String allowedHeaders = "*";

    /** Whether to allow credentials (cookies, authorization headers). Default: true */
    private Boolean allowCredentials = true;

    /** Max age for CORS preflight responses in seconds. Default: 3600 (1 hour) */
    private Long maxAge = 3600L;
}
