package com.kanwise.user_service.controller.kafka;

import lombok.experimental.UtilityClass;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS;
import static org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS;
import static org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES;


@UtilityClass
public class KafkaTestingUtils {

    public static Map<String, Object> getKafkaConsumerProperties(String bootstrapServers) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        properties.put(GROUP_ID_CONFIG, "producer");
        properties.put(KEY_DESERIALIZER_CLASS, ErrorHandlingDeserializer.class);
        properties.put(VALUE_DESERIALIZER_CLASS, ErrorHandlingDeserializer.class);
        properties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(TRUSTED_PACKAGES, "*");
        return properties;
    }
}
