package com.kanwise.api_gateway.configuration.gateway;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.time.Duration.ofSeconds;

@Configuration
public class CircuitBreakerConfiguration {

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(getCircuitBreakerConfiguration())
                .timeLimiterConfig(getTimeLimiterConfiguration())
                .build()
        );
    }

    private CircuitBreakerConfig getCircuitBreakerConfiguration() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .permittedNumberOfCallsInHalfOpenState(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(ofSeconds(30))
                .build();
    }

    private TimeLimiterConfig getTimeLimiterConfiguration() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(ofSeconds(5))
                .build();
    }
}