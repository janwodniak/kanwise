package com.kanwise.notification_service.listeners;

import lombok.experimental.UtilityClass;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.testcontainers.containers.KafkaContainer;

import java.util.Map;

import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

@UtilityClass
public class KafkaTestingUtils {

    public static Map<String, Object> getKafkaProducerProperties(KafkaContainer kafkaContainer) {
        return Map.of(
                BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    }
}
