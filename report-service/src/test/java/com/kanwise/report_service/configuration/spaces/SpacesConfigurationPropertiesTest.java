package com.kanwise.report_service.configuration.spaces;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = SpacesConfigurationPropertiesTest.class)
@ConfigurationPropertiesScan
@ActiveProfiles("test")
class SpacesConfigurationPropertiesTest {

    @Autowired
    private SpacesConfigurationProperties spacesConfigurationProperties;

    @Test
    void shouldPopulateSpacesConfigurationProperties() {
        // Given
        // When
        // Then
        assertEquals("nyc3.digitaloceanspaces.com", spacesConfigurationProperties.serviceEndpoint());
        assertEquals("MY_SECRET_KEY", spacesConfigurationProperties.secretKey());
        assertEquals("nyc3", spacesConfigurationProperties.signingRegion());
        assertEquals("MY_ACCESS_KEY", spacesConfigurationProperties.accessKey());
    }
}