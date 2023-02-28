package com.kanwise.notification_service.configuration.kafka.common;

import com.kanwise.notification_service.NotificationServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.kanwise.notification_service.model.kafka.TopicType.NOTIFICATION_EMAIL;
import static com.kanwise.notification_service.model.kafka.TopicType.NOTIFICATION_SMS;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(classes = NotificationServiceApplication.class)
@ActiveProfiles("test-kafka-disabled")
class KafkaConfigurationPropertiesTest {

    @Autowired
    private KafkaConfigurationProperties kafkaConfigurationProperties;

    @Test
    void shouldPopulateKafkaConfigurationProperties() {
        // Given
        // When
        // Then
        assertEquals("localhost:29092", kafkaConfigurationProperties.bootstrapServers());
        assertEquals("notification-email", kafkaConfigurationProperties.topicNames().get(NOTIFICATION_EMAIL));
        assertEquals("notification-sms", kafkaConfigurationProperties.getTopicName(NOTIFICATION_SMS));
    }
}