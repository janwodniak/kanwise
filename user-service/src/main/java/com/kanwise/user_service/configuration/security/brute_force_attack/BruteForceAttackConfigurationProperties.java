package com.kanwise.user_service.configuration.security.brute_force_attack;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

@Validated
@ConfigurationProperties("kanwise.brute-force-attack")
public record BruteForceAttackConfigurationProperties(
        @Min(1) int maximumNumberOfAttempts,
        @Min(1) int attemptIncrement,
        @Min(1) int expireAfterWriteUnit,
        @NotNull TimeUnit expireAfterWriteTimeUnit
) {
}
