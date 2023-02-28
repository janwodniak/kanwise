package com.kanwise.user_service.configuration.kafka.producer;

import com.kanwise.user_service.configuration.kafka.common.KafkaConfigurationProperties;
import com.kanwise.user_service.model.notification.email.EmailRequest;
import com.kanwise.user_service.model.notification.sms.OtpSmsRequest;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

@RequiredArgsConstructor
@Configuration
public class KafkaProducerConfiguration {

    private final KafkaConfigurationProperties kafkaConfigurationProperties;

    public Map<String, Object> producerConfiguration() {
        Map<String, Object> props = new HashMap<>();
        props.put(BOOTSTRAP_SERVERS_CONFIG, kafkaConfigurationProperties.bootstrapServers());
        props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, EmailRequest> producerEmailFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfiguration());
    }

    @Bean
    public KafkaTemplate<String, EmailRequest> kafkaEmailTemplate(ProducerFactory<String, EmailRequest> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, OtpSmsRequest> producerSmsFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfiguration());
    }

    @Bean
    public KafkaTemplate<String, OtpSmsRequest> kafkaSmsTemplate(ProducerFactory<String, OtpSmsRequest> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
