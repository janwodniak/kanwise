package com.kanwise.user_service.configuration.security.brute_force_attack;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = BruteForceAttackConfigurationPropertiesTest.class)
@ConfigurationPropertiesScan
@ActiveProfiles("test")
class BruteForceAttackConfigurationPropertiesTest {

    @Autowired
    private BruteForceAttackConfigurationProperties bruteForceAttackConfigurationProperties;

    @Test
    void shouldPopulateBruteForceAttackConfigurationProperties() {
        // Given
        // When
        // Then
        assertEquals(5, bruteForceAttackConfigurationProperties.maximumNumberOfAttempts());
        assertEquals(1, bruteForceAttackConfigurationProperties.attemptIncrement());
        assertEquals(15, bruteForceAttackConfigurationProperties.expireAfterWriteUnit());
        assertEquals(MINUTES, bruteForceAttackConfigurationProperties.expireAfterWriteTimeUnit());
    }
}