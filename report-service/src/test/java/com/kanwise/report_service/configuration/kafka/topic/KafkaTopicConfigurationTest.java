//package com.kanwise.report_service.configuration.kafka.topic;
//
//import com.kanwise.report_service.configuration.kafka.general.KafkaConfigurationProperties;
//import org.apache.kafka.clients.admin.NewTopic;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.ApplicationContext;
//import org.springframework.test.context.ActiveProfiles;
//
//import static com.kanwise.report_service.model.kafka.TopicType.NOTIFICATION_EMAIL;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest(classes = KafkaTopicConfiguration.class)
//@ActiveProfiles("test")
//class KafkaTopicConfigurationTest {
//
//
//    private final KafkaTopicConfiguration kafkaTopicConfiguration;
//
//    private final ApplicationContext applicationContext;
//
//    @MockBean
//    private KafkaConfigurationProperties kafkaConfigurationProperties;
//
//    @Autowired
//    KafkaTopicConfigurationTest(KafkaTopicConfiguration kafkaTopicConfiguration, ApplicationContext applicationContext) {
//        this.kafkaTopicConfiguration = kafkaTopicConfiguration;
//        this.applicationContext = applicationContext;
//    }
//
//    @Test
//    void shouldPopulateKafkaNotificationEmailTopic() {
//        // Given
//        String topicName = "notification-email";
//        when(kafkaConfigurationProperties.getTopicName(NOTIFICATION_EMAIL)).thenReturn(topicName);
//        // When
//        NewTopic notificationEmailTopic = kafkaTopicConfiguration.notificationEmailTopic();
//        Object notificationEmailTopicBean = applicationContext.getBean("notificationEmailTopic");
//        // Then
//        assertNotNull(notificationEmailTopicBean);
//        assertNotNull(notificationEmailTopic);
//        assertEquals(topicName, notificationEmailTopic.name());
//        assertEquals(notificationEmailTopicBean, notificationEmailTopic);
//    }
//}