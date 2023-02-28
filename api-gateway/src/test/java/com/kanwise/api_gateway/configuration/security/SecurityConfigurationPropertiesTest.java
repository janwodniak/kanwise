package com.kanwise.api_gateway.configuration.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ConfigurationPropertiesScan
@ActiveProfiles("test")
class SecurityConfigurationPropertiesTest {

    @Autowired
    private SecurityConfigurationProperties securityConfigurationProperties;

    @Test
    void shouldPopulateAuthenticationRoute() {
        // Given
        // When
        // Then
        assertNotNull(securityConfigurationProperties.authenticationRoute());
        assertEquals("http://user-service/auth/token/validate", securityConfigurationProperties.authenticationRoute());
    }
}