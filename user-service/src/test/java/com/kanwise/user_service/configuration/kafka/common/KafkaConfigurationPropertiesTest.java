package com.kanwise.user_service.configuration.kafka.common;

import com.kanwise.user_service.model.kafka.TopicType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ConfigurationPropertiesScan
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
        assertEquals("notification-email", kafkaConfigurationProperties.topicNames().get(TopicType.NOTIFICATION_EMAIL));
        assertEquals("notification-sms", kafkaConfigurationProperties.getTopicName(TopicType.NOTIFICATION_SMS));
    }
}