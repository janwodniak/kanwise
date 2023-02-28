package com.kanwise.notification_service.configuration.email;

import com.kanwise.notification_service.NotificationServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = NotificationServiceApplication.class)
@ActiveProfiles("test-kafka-disabled")
class DirectoryConfigurationPropertiesTest {

    @Autowired
    private DirectoryConfigurationProperties directoryConfigurationProperties;

    @Test
    void shouldEmailTemplatesDirectory() {
        // Given
        // When
        // Then
        assertEquals("src/test/resources/templates", directoryConfigurationProperties.emailTemplates());
    }
}