package com.kanwise.report_service.controller.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testcontainers.utility.DockerImageName.parse;

@SpringBootTest
@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
class KafkaTopicCreationIT {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(parse("confluentinc/cp-kafka:latest"));
    @Autowired
    private KafkaAdmin admin;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Test
    void testCreationOfTopicAtStartup() throws InterruptedException, ExecutionException {
        try (AdminClient client = AdminClient.create(admin.getConfigurationProperties())) {
            Collection<TopicListing> topicList = client.listTopics().listings().get();
            assertNotNull(topicList);
            assertEquals(List.of("notification-email"), topicList.stream().map(TopicListing::name).toList());
        }
    }
}
