package com.kanwise.notification_service.configuration.kafka.consumer;

import com.kanwise.notification_service.NotificationServiceApplication;
import com.kanwise.notification_service.model.email.EmailRequest;
import com.kanwise.notification_service.model.sms.OtpSmsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.core.ResolvableType.forType;

@SpringBootTest(classes = NotificationServiceApplication.class)
@ActiveProfiles("test-kafka-disabled")
class KafkaConsumerConfigurationTest {

    private final ApplicationContext applicationContext;
    private final KafkaConsumerConfiguration kafkaConsumerConfiguration;


    @Autowired
    public KafkaConsumerConfigurationTest(ApplicationContext applicationContext, KafkaConsumerConfiguration kafkaConsumerConfiguration) {
        this.applicationContext = applicationContext;
        this.kafkaConsumerConfiguration = kafkaConsumerConfiguration;
    }

    @Test
    void shouldPopulateConsumerConfiguration() {
        // Given
        // When
        Map<String, Object> consumerConfiguration = kafkaConsumerConfiguration.consumerConfiguration();
        // Then
        assertNotNull(consumerConfiguration);
        assertEquals(1, consumerConfiguration.size());
        assertEquals(("localhost:29092"), consumerConfiguration.get(BOOTSTRAP_SERVERS_CONFIG));
    }

    @Test
    void shouldPopulateConsumerEmailFactory() {
        // Given
        // When
        ConsumerFactory<String, EmailRequest> consumerEmailFactory = kafkaConsumerConfiguration.consumerEmailFactory();
        String[] beanNames = applicationContext.getBeanNamesForType(forType(new ParameterizedTypeReference<ConsumerFactory<String, EmailRequest>>() {
        }));
        Object consumerEmailFactoryBean = applicationContext.getBean(beanNames[0]);
        // Then
        assertNotNull(consumerEmailFactory);
        assertEquals(1, beanNames.length);
        assertEquals(consumerEmailFactory, consumerEmailFactoryBean);
        assertEquals(DefaultKafkaConsumerFactory.class, consumerEmailFactory.getClass());
    }

    @Test
    void shouldPopulateEmailFactory() {
        // Given
        // When
        KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, EmailRequest>> emailFactory = kafkaConsumerConfiguration.emailFactory(kafkaConsumerConfiguration.consumerEmailFactory());
        String[] beanNames = applicationContext.getBeanNamesForType(forType(new ParameterizedTypeReference<KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, EmailRequest>>>() {
        }));
        Object emailFactoryBean = applicationContext.getBean(beanNames[0]);
        // Then
        assertNotNull(emailFactory);
        assertEquals(1, beanNames.length);
        assertEquals(emailFactory, emailFactoryBean);
        assertEquals(ConcurrentKafkaListenerContainerFactory.class, emailFactory.getClass());
    }

    @Test
    void shouldPopulateConsumerSmsFactory() {
        // Given
        // When
        ConsumerFactory<String, OtpSmsRequest> consumerSmsFactory = kafkaConsumerConfiguration.consumerSmsFactory();
        String[] beanNames = applicationContext.getBeanNamesForType(forType(new ParameterizedTypeReference<ConsumerFactory<String, OtpSmsRequest>>() {
        }));
        Object consumerSmsFactoryBean = applicationContext.getBean(beanNames[0]);
        // Then
        assertNotNull(consumerSmsFactory);
        assertEquals(1, beanNames.length);
        assertEquals(consumerSmsFactory, consumerSmsFactoryBean);
        assertEquals(DefaultKafkaConsumerFactory.class, consumerSmsFactory.getClass());
    }

    @Test
    void shouldPopulateSmsFactory() {
        // Given
        // When
        KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, OtpSmsRequest>> smsFactory = kafkaConsumerConfiguration.smsFactory(kafkaConsumerConfiguration.consumerSmsFactory());
        String[] beanNames = applicationContext.getBeanNamesForType(forType(new ParameterizedTypeReference<KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, OtpSmsRequest>>>() {
        }));
        Object smsFactoryBean = applicationContext.getBean(beanNames[0]);
        // Then
        assertNotNull(smsFactory);
        assertEquals(1, beanNames.length);
        assertEquals(smsFactory, smsFactoryBean);
        assertEquals(ConcurrentKafkaListenerContainerFactory.class, smsFactory.getClass());
    }
}