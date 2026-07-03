package com.noom.interview.fullstack.sleep.config;

import
        java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /**
     * A {@link Clock} bean so that "today" is injectable and business logic that depends on
     * the current date can be tested deterministically.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
