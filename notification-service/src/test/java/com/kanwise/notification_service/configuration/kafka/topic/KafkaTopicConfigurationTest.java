package com.kanwise.notification_service.configuration.kafka.topic;

import com.kanwise.notification_service.NotificationServiceApplication;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = NotificationServiceApplication.class)
@ActiveProfiles("test-kafka-disabled")
class KafkaTopicConfigurationTest {

    private final KafkaTopicConfiguration kafkaTopicConfiguration;

    private final ApplicationContext applicationContext;


    @Autowired
    KafkaTopicConfigurationTest(KafkaTopicConfiguration kafkaTopicConfiguration, ApplicationContext applicationContext) {
        this.kafkaTopicConfiguration = kafkaTopicConfiguration;
        this.applicationContext = applicationContext;
    }

    @Test
    void shouldPopulateKafkaNotificationEmailTopic() {
        // Given
        String topicName = "notification-email";
        // When
        NewTopic notificationEmailTopic = kafkaTopicConfiguration.notificationEmailTopic();
        Object notificationEmailTopicBean = applicationContext.getBean("notificationEmailTopic");
        // Then
        assertNotNull(notificationEmailTopicBean);
        assertNotNull(notificationEmailTopic);
        assertEquals(topicName, notificationEmailTopic.name());
        assertEquals(notificationEmailTopicBean, notificationEmailTopic);
    }

    @Test
    void shouldNotificationSmsTopic() {
        // Given
        String topicName = "notification-sms";
        // When
        NewTopic notificationSmsTopic = kafkaTopicConfiguration.notificationSmsTopic();
        Object notificationSmsTopicBean = applicationContext.getBean("notificationSmsTopic");
        // Then
        assertNotNull(notificationSmsTopicBean);
        assertNotNull(notificationSmsTopic);
        assertEquals(topicName, notificationSmsTopic.name());
        assertEquals(notificationSmsTopicBean, notificationSmsTopic);
    }
}