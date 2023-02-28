package com.kanwise.kanwise_service.configuration.common;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    void shouldPopulateModelMapper() {
        // Given
        // When
        // Then
        assertNotNull(applicationConfiguration.modelMapper(Set.of()));
        assertNotNull(applicationContext.getBean(ModelMapper.class));
    }

    @Test
    void shouldPopulateClock() {
        // Given
        // When
        // Then
        assertNotNull(applicationConfiguration.clock());
        assertNotNull(applicationContext.getBean(Clock.class));
    }
}