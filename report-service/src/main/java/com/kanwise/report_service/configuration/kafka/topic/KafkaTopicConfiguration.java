package com.kanwise.report_service.configuration.kafka.topic;

import com.kanwise.report_service.configuration.kafka.common.KafkaConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static com.kanwise.report_service.model.kafka.TopicType.NOTIFICATION_EMAIL;

@RequiredArgsConstructor
@Configuration
public class KafkaTopicConfiguration {

    private final KafkaConfigurationProperties kafkaConfigurationProperties;

    @Bean
    public NewTopic notificationEmailTopic() {
        return TopicBuilder.name(kafkaConfigurationProperties.getTopicName(NOTIFICATION_EMAIL))
                .build();
    }
}
