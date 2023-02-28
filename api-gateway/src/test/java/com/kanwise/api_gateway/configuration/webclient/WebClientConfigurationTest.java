package com.kanwise.api_gateway.configuration.webclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class WebClientConfigurationTest {

    private final WebClientConfiguration webClientConfiguration;
    private final ApplicationContext applicationContext;

    @Autowired
    WebClientConfigurationTest(WebClientConfiguration webClientConfiguration, ApplicationContext applicationContext) {
        this.webClientConfiguration = webClientConfiguration;
        this.applicationContext = applicationContext;
    }

    @Test
    void shouldPopulateWebClientBuilder() {
        // Given
        // When
        WebClient.Builder webClientBuilder = webClientConfiguration.loadBalancedWebClientBuilder();
        WebClient.Builder webClientBuilderBean = applicationContext.getBean(WebClient.Builder.class);
        // Then
        assertNotNull(webClientBuilder);
        assertNotNull(webClientBuilderBean);
        assertEquals(webClientBuilder, webClientBuilderBean);
    }
}