package com.kanwise.report_service.configuration.kafka.common;

import com.kanwise.report_service.model.kafka.TopicType;
import com.kanwise.report_service.validation.annotation.kafka.TopicNames;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Validated
@ConfigurationProperties("spring.kafka")
public record KafkaConfigurationProperties(
        @NotEmpty(message = "BOOTSTRAP_SERVERS_NOT_EMPTY") String bootstrapServers,
        @TopicNames Map<TopicType, String> topicNames
) {
    public String getTopicName(TopicType topicType) {
        return topicNames.get(topicType);
    }
}

