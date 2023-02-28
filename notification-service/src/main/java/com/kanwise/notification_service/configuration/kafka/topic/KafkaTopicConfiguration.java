package com.kanwise.notification_service.configuration.kafka.topic;

import com.kanwise.notification_service.configuration.kafka.common.KafkaConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static com.kanwise.notification_service.model.kafka.TopicType.NOTIFICATION_EMAIL;
import static com.kanwise.notification_service.model.kafka.TopicType.NOTIFICATION_SMS;

@RequiredArgsConstructor
@Configuration
public class KafkaTopicConfiguration {

    private final KafkaConfigurationProperties kafkaConfigurationProperties;

    @Bean
    public NewTopic notificationEmailTopic() {
        return TopicBuilder.name(kafkaConfigurationProperties.getTopicName(NOTIFICATION_EMAIL))
                .build();
    }

    @Bean
    public NewTopic notificationSmsTopic() {
        return TopicBuilder.name(kafkaConfigurationProperties.getTopicName(NOTIFICATION_SMS))
                .build();
    }
}
