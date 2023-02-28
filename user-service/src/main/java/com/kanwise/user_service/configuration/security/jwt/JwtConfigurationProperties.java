package com.kanwise.user_service.configuration.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;


@Validated
@ConfigurationProperties(prefix = "kanwise.jwt")
public record JwtConfigurationProperties(
        @NotEmpty(message = "SECRET_KEY_NOT_EMPTY") String secretKey,
        @NotEmpty(message = "TOKEN_PREFIX_NOT_EMPTY") String tokenPrefix,
        @NotNull(message = "DURATION_NOT_NULL") Duration expirationAfter,
        @NotEmpty(message = "AUTHORITIES_NOT_EMPTY") String authorities,
        @NotEmpty(message = "ISSUER_NOT_EMPTY") String issuer,
        @NotEmpty(message = "AUDIENCE_NOT_EMPTY") String audience) {
}
