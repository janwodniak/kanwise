package com.kanwise.user_service.configuration.spaces;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = SpacesInitializer.class)
@ActiveProfiles("test")
class SpacesInitializerTest {

    private final SpacesInitializer spacesInitializer;
    private final ApplicationContext applicationContext;
    @MockBean
    private SpacesConfigurationProperties spacesConfigurationProperties;

    @Autowired
    SpacesInitializerTest(SpacesInitializer spacesInitializer, ApplicationContext applicationContext) {
        this.spacesInitializer = spacesInitializer;
        this.applicationContext = applicationContext;
    }

    @Test
    void shouldPopulateSpace() {
        // Given
        when(spacesConfigurationProperties.accessKey()).thenReturn("accessKey");
        when(spacesConfigurationProperties.secretKey()).thenReturn("secretKey");
        when(spacesConfigurationProperties.serviceEndpoint()).thenReturn("serviceEndpoint");
        when(spacesConfigurationProperties.signingRegion()).thenReturn("signingRegion");
        // When
        AmazonS3 space = spacesInitializer.space();
        AmazonS3 spaceBean = applicationContext.getBean(AmazonS3.class);
        // Then
        assertNotNull(space);
        assertNotNull(spaceBean);
        assertEquals(space, spaceBean);
    }
}