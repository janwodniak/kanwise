package com.kanwise.api_gateway.configuration.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = JacksonConfiguration.class)
@ActiveProfiles("test")
class JacksonConfigurationTest {

    private final JacksonConfiguration jacksonConfiguration;
    private final ApplicationContext applicationContext;

    @Autowired
    public JacksonConfigurationTest(JacksonConfiguration jacksonConfiguration, ApplicationContext applicationContext) {
        this.jacksonConfiguration = jacksonConfiguration;
        this.applicationContext = applicationContext;
    }

    @Test
    void shouldPopulateObjectMapper() {
        // Given
        // When
        ObjectMapper objectMapper = jacksonConfiguration.objectMapper();
        ObjectMapper objectMapperBean = applicationContext.getBean(ObjectMapper.class);
        // Then
        assertNotNull(objectMapper);
        assertNotNull(objectMapperBean);
        assertEquals(objectMapper, objectMapperBean);
        assertFalse(objectMapper.getDeserializationConfig().isEnabled(FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(objectMapper.getSerializationConfig().isEnabled(WRITE_DATES_AS_TIMESTAMPS));
    }
}