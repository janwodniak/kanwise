package com.kanwise.api_gateway.configuration.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableConfigurationProperties
@SpringBootTest
@ActiveProfiles("test")
class CustomCorsConfigurationTest {

    private final CustomCorsConfiguration customCorsConfiguration;
    private final ApplicationContext applicationContext;

    @Autowired
    public CustomCorsConfigurationTest(CustomCorsConfiguration customCorsConfiguration, ApplicationContext applicationContext) {
        this.customCorsConfiguration = customCorsConfiguration;
        this.applicationContext = applicationContext;
    }

    @Test
    void shouldPopulateCorsWebFilter() {
        // Given
        // When
        CorsWebFilter corsWebFilter = customCorsConfiguration.corsWebFilter();
        CorsWebFilter corsWebFilterBean = applicationContext.getBean(CorsWebFilter.class);
        // Then
        assertNotNull(corsWebFilter);
        assertNotNull(corsWebFilterBean);
        assertEquals(corsWebFilter, corsWebFilterBean);
    }
}