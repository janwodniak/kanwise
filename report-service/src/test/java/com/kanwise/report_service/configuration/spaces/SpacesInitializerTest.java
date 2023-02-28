package com.kanwise.report_service.configuration.spaces;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class SpacesInitializerTest {

    private final SpacesInitializer spacesInitializer;
    private final ApplicationContext applicationContext;

    @Autowired
    SpacesInitializerTest(SpacesInitializer spacesInitializer, ApplicationContext applicationContext) {
        this.spacesInitializer = spacesInitializer;
        this.applicationContext = applicationContext;
    }

    @Test
    void shouldPopulateSpace() {
        // Given
        // When
        AmazonS3 space = spacesInitializer.space();
        AmazonS3 spaceBean = applicationContext.getBean(AmazonS3.class);
        // Then
        assertNotNull(space);
        assertNotNull(spaceBean);
        assertEquals(space, spaceBean);
    }
}