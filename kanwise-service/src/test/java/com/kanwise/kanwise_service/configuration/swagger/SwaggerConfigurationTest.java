package com.kanwise.kanwise_service.configuration.swagger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import springfox.documentation.spring.web.plugins.Docket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = SwaggerConfiguration.class)
@ActiveProfiles("test")
class SwaggerConfigurationTest {

    private final SwaggerConfiguration swaggerConfiguration;
    private final ApplicationContext applicationContext;

    @Autowired
    SwaggerConfigurationTest(SwaggerConfiguration swaggerConfiguration, ApplicationContext applicationContext) {
        this.swaggerConfiguration = swaggerConfiguration;
        this.applicationContext = applicationContext;
    }

    @Test
    void shouldPopulateDocket() {
        // Given
        // When
        Docket docket = swaggerConfiguration.docket();
        Docket docketBean = applicationContext.getBean(Docket.class);
        // Then
        assertNotNull(docket);
        assertNotNull(docketBean);
        assertEquals(docket, docketBean);
    }
}