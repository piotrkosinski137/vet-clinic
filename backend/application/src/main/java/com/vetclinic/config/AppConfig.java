package com.vetclinic.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Application-wide configuration for common beans. */
@Configuration
public class AppConfig {

    /**
     * Provides a system Clock bean for time-dependent operations. Using an injectable Clock instead
     * of LocalDate.now() or LocalDateTime.now() enables: - Easier unit testing with fixed/mock
     * clocks - Consistent time across the application - Timezone awareness when needed
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
