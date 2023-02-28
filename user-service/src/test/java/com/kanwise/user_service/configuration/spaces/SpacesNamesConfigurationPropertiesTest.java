package com.kanwise.user_service.configuration.spaces;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = SpacesConfigurationPropertiesTest.class)
@ConfigurationPropertiesScan
@ActiveProfiles("test")
class SpacesNamesConfigurationPropertiesTest {

    @Autowired
    private SpacesNamesConfigurationProperties spacesNamesConfigurationProperties;

    @Test
    void shouldPopulateSpacesConfigurationProperties() {
        // Given
        // When
        // Then
        assertEquals("kanwise", spacesNamesConfigurationProperties.profileImages());
    }
}