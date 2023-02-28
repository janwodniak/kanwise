package com.kanwise.user_service.configuration.security.password_reset_token;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.Duration;

@Validated
@ConfigurationProperties("kanwise.token")
public record PasswordResetTokenConfigurationProperties(
        @NotNull(message = "DURATION_NOT_NULL") Duration expiration
) {
}
