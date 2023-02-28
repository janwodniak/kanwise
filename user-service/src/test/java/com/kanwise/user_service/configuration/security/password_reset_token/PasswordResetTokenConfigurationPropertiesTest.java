package com.kanwise.user_service.configuration.security.password_reset_token;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static java.time.Duration.ofHours;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = PasswordResetTokenConfigurationPropertiesTest.class)
@ConfigurationPropertiesScan
@ActiveProfiles("test")
class PasswordResetTokenConfigurationPropertiesTest {

    @Autowired
    private PasswordResetTokenConfigurationProperties passwordResetTokenConfigurationProperties;

    @Test
    void shouldPopulateJwtConfigurationProperties() {
        // Given
        // When
        // Then
        assertThat(passwordResetTokenConfigurationProperties.expiration()).isEqualTo(ofHours(1));
    }
}