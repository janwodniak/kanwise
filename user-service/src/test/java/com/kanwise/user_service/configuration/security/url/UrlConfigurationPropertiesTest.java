package com.kanwise.user_service.configuration.security.url;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = UrlConfigurationPropertiesTest.class)
@ConfigurationPropertiesScan
@ActiveProfiles("test")
class UrlConfigurationPropertiesTest {

    private final UrlConfigurationProperties urlConfigurationProperties;

    @Autowired
    UrlConfigurationPropertiesTest(UrlConfigurationProperties urlConfigurationProperties) {
        this.urlConfigurationProperties = urlConfigurationProperties;
    }

    @Test
    void shouldPopulateUrlConfigurationProperties() {
        // Given
        // When
        List<String> publicUrls = urlConfigurationProperties.publicUrls();
        // Then
        assertEquals(1, publicUrls.size());
        assertEquals("**", publicUrls.get(0));
    }

    @Test
    void shouldGetPublicUrlsAsArray() {
        // Given
        // When
        String[] publicUrls = urlConfigurationProperties.publicUrlsAsArray();
        // Then
        assertEquals(1, publicUrls.length);
        assertEquals("**", publicUrls[0]);
    }
}