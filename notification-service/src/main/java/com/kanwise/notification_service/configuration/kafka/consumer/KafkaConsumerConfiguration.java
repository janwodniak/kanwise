package com.kanwise.notification_service.configuration.kafka.consumer;


import com.kanwise.notification_service.configuration.kafka.common.KafkaConfigurationProperties;
import com.kanwise.notification_service.model.email.EmailRequest;
import com.kanwise.notification_service.model.sms.OtpSmsRequest;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;

@RequiredArgsConstructor
@Configuration
public class KafkaConsumerConfiguration {

    private final KafkaConfigurationProperties kafkaConfigurationProperties;

    public Map<String, Object> consumerConfiguration() {
        return Map.of(BOOTSTRAP_SERVERS_CONFIG, kafkaConfigurationProperties.bootstrapServers());
    }

    @Bean
    public ConsumerFactory<String, EmailRequest> consumerEmailFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfiguration(), new StringDeserializer(), new JsonDeserializer<>(EmailRequest.class, false));
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, EmailRequest>> emailFactory(ConsumerFactory<String, EmailRequest> consumerEmailFactory) {
        ConcurrentKafkaListenerContainerFactory<String, EmailRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerEmailFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, OtpSmsRequest> consumerSmsFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfiguration(), new StringDeserializer(), new JsonDeserializer<>(OtpSmsRequest.class, false));
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, OtpSmsRequest>> smsFactory(ConsumerFactory<String, OtpSmsRequest> consumerSmsFactory) {
        ConcurrentKafkaListenerContainerFactory<String, OtpSmsRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerSmsFactory);
        return factory;
    }
}
