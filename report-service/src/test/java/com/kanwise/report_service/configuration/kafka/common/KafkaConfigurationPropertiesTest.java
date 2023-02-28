//package com.kanwise.report_service.configuration.kafka.general;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import static com.kanwise.report_service.model.kafka.TopicType.NOTIFICATION_EMAIL;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//@SpringBootTest
//@ConfigurationPropertiesScan
//@ActiveProfiles("test-kafka-db-disabled")
//class KafkaConfigurationPropertiesTest {
//
//    @Autowired
//    private KafkaConfigurationProperties kafkaConfigurationProperties;
//
//    @Test
//    void shouldPopulateKafkaConfigurationProperties() {
//        // Given
//        // When
//        // Then
//        assertEquals("localhost:29092", kafkaConfigurationProperties.bootstrapServers());
//        assertEquals("notification-email", kafkaConfigurationProperties.topicNames().get(NOTIFICATION_EMAIL));
//    }
//}