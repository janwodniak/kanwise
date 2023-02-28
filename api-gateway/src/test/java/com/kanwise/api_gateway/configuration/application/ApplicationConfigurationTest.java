package com.kanwise.api_gateway.configuration.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ApplicationConfiguration.class)
@ActiveProfiles("test")
class ApplicationConfigurationTest {

    private final ApplicationConfiguration applicationConfiguration;
    private final ApplicationContext applicationContext;

    @Autowired
    public ApplicationConfigurationTest(ApplicationConfiguration applicationConfiguration, ApplicationContext applicationContext) {
        this.applicationConfiguration = applicationConfiguration;
        this.applicationContext = applicationContext;
    }

    @Test
    void shouldPopulateClock() {
        // Given
        // When
        Clock clock = applicationConfiguration.clock();
        Clock clockBean = applicationContext.getBean(Clock.class);
        // Then
        assertNotNull(clock);
        assertNotNull(clockBean);
        assertEquals(clock, clockBean);
    }
}