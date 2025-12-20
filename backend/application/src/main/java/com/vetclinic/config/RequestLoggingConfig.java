package com.vetclinic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/** Configures request/response logging for debugging and audit purposes. */
@Configuration
public class RequestLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(true);
        filter.setHeaderPredicate(
                headerName ->
                        !headerName.equalsIgnoreCase("Authorization")
                                && !headerName.equalsIgnoreCase("Cookie"));
        filter.setAfterMessagePrefix("REQUEST: ");
        return filter;
    }
}
