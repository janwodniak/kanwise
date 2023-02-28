//package com.kanwise.report_service.configuration.kafka.producer;
//
//import com.kanwise.report_service.configuration.kafka.general.KafkaConfigurationProperties;
//import com.kanwise.report_service.model.notification.email.EmailRequest;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.ApplicationContext;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//import org.springframework.kafka.support.serializer.JsonSerializer;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.Map;
//
//import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
//import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
//import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.when;
//import static org.springframework.core.ResolvableType.forType;
//
//@SpringBootTest(classes = KafkaProducerConfiguration.class)
//@ActiveProfiles("test")
//class KafkaProducerConfigurationTest {
//    private final ApplicationContext applicationContext;
//
//    private final KafkaProducerConfiguration kafkaProducerConfiguration;
//
//    @MockBean
//    private KafkaConfigurationProperties kafkaConfigurationProperties;
//
//    @Autowired
//    public KafkaProducerConfigurationTest(ApplicationContext applicationContext, KafkaProducerConfiguration kafkaProducerConfiguration) {
//        this.applicationContext = applicationContext;
//        this.kafkaProducerConfiguration = kafkaProducerConfiguration;
//    }
//
//    @Test
//    void shouldPopulateProducerEmailFactory() {
//        // Given
//        when(kafkaConfigurationProperties.bootstrapServers()).thenReturn("localhost:29092");
//        // When
//        ProducerFactory<String, EmailRequest> producerEmailFactory = kafkaProducerConfiguration.producerEmailFactory();
//        String[] beanNames = applicationContext.getBeanNamesForType(forType(new ParameterizedTypeReference<ProducerFactory<String, EmailRequest>>() {
//        }));
//        Object produceEmailFactoryBean = applicationContext.getBean(beanNames[0]);
//        // Then
//        assertNotNull(producerEmailFactory);
//        assertEquals(1, beanNames.length);
//        assertNotNull(produceEmailFactoryBean);
//        assertEquals(DefaultKafkaProducerFactory.class, produceEmailFactoryBean.getClass());
//    }
//
//    @Test
//    void shouldPopulateKafkaEmailTemplate() {
//        // Given
//        when(kafkaConfigurationProperties.bootstrapServers()).thenReturn("localhost:29092");
//        // When
//        KafkaTemplate<String, EmailRequest> kafkaEmailTemplate = kafkaProducerConfiguration.kafkaEmailTemplate(kafkaProducerConfiguration.producerEmailFactory());
//        String[] beanNames = applicationContext.getBeanNamesForType(forType(new ParameterizedTypeReference<KafkaTemplate<String, EmailRequest>>() {
//        }));
//        Object kafkaEmailTemplateBean = applicationContext.getBean(beanNames[0]);
//        // Then
//        assertNotNull(kafkaEmailTemplate);
//        assertEquals(1, beanNames.length);
//        assertNotNull(kafkaEmailTemplateBean);
//        assertEquals(KafkaTemplate.class, kafkaEmailTemplateBean.getClass());
//    }
//
//    @Test
//    void shouldPopulateProducerConfiguration() {
//        // Given
//        when(kafkaConfigurationProperties.bootstrapServers()).thenReturn("localhost:29092");
//        // When
//        Map<String, Object> stringObjectMap = kafkaProducerConfiguration.producerConfiguration();
//        // Then
//        assertEquals(3, stringObjectMap.size());
//        assertEquals("localhost:29092", stringObjectMap.get(BOOTSTRAP_SERVERS_CONFIG));
//        assertEquals(StringSerializer.class, stringObjectMap.get(KEY_SERIALIZER_CLASS_CONFIG));
//        assertEquals(JsonSerializer.class, stringObjectMap.get(VALUE_SERIALIZER_CLASS_CONFIG));
//    }
//}