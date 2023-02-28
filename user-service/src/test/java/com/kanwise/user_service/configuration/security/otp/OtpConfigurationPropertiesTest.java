package com.kanwise.user_service.configuration.security.otp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = OtpConfigurationPropertiesTest.class)
@ConfigurationPropertiesScan
@ActiveProfiles("test")
class OtpConfigurationPropertiesTest {

    @Autowired
    private OtpConfigurationProperties otpConfigurationProperties;

    @Test
    void shouldPopulateOtpConfigurationProperties() {
        // Given
        // When
        // Then
        assertEquals(6, otpConfigurationProperties.length());
        assertEquals(ofMinutes(5), otpConfigurationProperties.expiration());
    }
}