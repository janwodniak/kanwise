package com.kanwise.api_gateway.configuration.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ConfigurationPropertiesScan
@ActiveProfiles("test")
class CorsConfigurationPropertiesTest {

    @Autowired
    private CorsConfigurationProperties corsConfigurationProperties;

    @Test
    void shouldPopulateAllowedOrigins() {
        // Given
        // When
        // Then
        assertNotNull(corsConfigurationProperties.allowedOrigins());
        assertTrue(corsConfigurationProperties.allowedOrigins().contains("http://localhost:4200"));
    }
}