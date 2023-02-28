package com.kanwise.api_gateway.configuration.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static java.time.Clock.systemDefaultZone;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public Clock clock() {
        return systemDefaultZone();
    }
}
