package io.basat.monitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {


    // Code asks this bean for "now" instead of calling Instant.now(), so tests can fix the time.
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
