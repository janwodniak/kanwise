package com.kanwise.api_gateway.configuration.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class CircuitBreakerConfigurationTest {

    private final CircuitBreakerConfiguration circuitBreakerConfiguration;
    private final ApplicationContext applicationContext;

    @Autowired
    public CircuitBreakerConfigurationTest(CircuitBreakerConfiguration circuitBreakerConfiguration, ApplicationContext applicationContext) {
        this.circuitBreakerConfiguration = circuitBreakerConfiguration;
        this.applicationContext = applicationContext;
    }

    @Test
    void shouldPopulateCircuitBreakerCustomizer() {
        // Given
        // When
        Customizer<ReactiveResilience4JCircuitBreakerFactory> circuitBreakerFactoryCustomizer = circuitBreakerConfiguration.defaultCustomizer();
        Object circuitBreakerFactoryCustomizerBean = applicationContext.getBean("defaultCustomizer");
        // Then
        assertNotNull(circuitBreakerFactoryCustomizer);
        assertNotNull(circuitBreakerFactoryCustomizerBean);
        assertEquals(circuitBreakerFactoryCustomizer, circuitBreakerFactoryCustomizerBean);
    }
}