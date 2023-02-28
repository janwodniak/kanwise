package com.kanwise.notification_service.configuration.twillio;

import com.kanwise.notification_service.NotificationServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = NotificationServiceApplication.class)
@ActiveProfiles("test-kafka-disabled")
class TwilioConfigurationPropertiesTest {

    @Autowired
    private TwilioConfigurationProperties twilioConfigurationProperties;

    @Test
    void shouldPopulateTwilioConfigurationProperties() {
        // Given
        // When
        // Then
        assertEquals("testAccountSid", twilioConfigurationProperties.accountSid());
        assertEquals("testAuthToken", twilioConfigurationProperties.authToken());
        assertEquals("+48123456789", twilioConfigurationProperties.number());
    }
}