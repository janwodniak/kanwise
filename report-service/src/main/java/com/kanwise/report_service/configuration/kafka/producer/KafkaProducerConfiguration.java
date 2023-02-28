package com.kanwise.report_service.configuration.kafka.producer;


import com.kanwise.report_service.configuration.kafka.common.KafkaConfigurationProperties;
import com.kanwise.report_service.model.notification.email.EmailRequest;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

import static java.util.Map.of;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

@RequiredArgsConstructor
@Configuration
public class KafkaProducerConfiguration {

    private final KafkaConfigurationProperties kafkaConfigurationProperties;

    public Map<String, Object> producerConfiguration() {
        return of(
                BOOTSTRAP_SERVERS_CONFIG, kafkaConfigurationProperties.bootstrapServers(),
                KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        );
    }

    @Bean
    public ProducerFactory<String, EmailRequest> producerEmailFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfiguration());
    }

    @Bean
    public KafkaTemplate<String, EmailRequest> kafkaEmailTemplate(ProducerFactory<String, EmailRequest> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
