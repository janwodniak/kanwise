package com.kanwise.user_service.configuration.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static java.time.Duration.ofDays;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = JwtConfigurationPropertiesTest.class)
@ConfigurationPropertiesScan
@ActiveProfiles("test")
class JwtConfigurationPropertiesTest {

    @Autowired
    private JwtConfigurationProperties jwtConfigurationProperties;

    @Test
    void shouldPopulateJwtConfigurationProperties() {
        // Given
        // When
        // Then
        assertThat(jwtConfigurationProperties.secretKey()).isEqualTo("Yq3t6w9z$C&F)J@McQfTjWnZr4u7x!A%D*G-KaPdRgUkXp2s5v8y/B?E(H+MbQeThVmYq3t6w9z$C&F)J@NcRfUjXnZr4u7x!A%D*G-KaPdSgVkYp3s5v8y/B?E(H+Mb");
        assertThat(jwtConfigurationProperties.tokenPrefix()).isEqualTo("Bearer ");
        assertThat(jwtConfigurationProperties.expirationAfter()).isEqualTo(ofDays(20));
        assertThat(jwtConfigurationProperties.authorities()).isEqualTo("kanwise");
        assertThat(jwtConfigurationProperties.issuer()).isEqualTo("kanwise.com");
        assertThat(jwtConfigurationProperties.audience()).isEqualTo("kanwise");
    }
}