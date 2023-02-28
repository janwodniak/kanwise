package com.kanwise.user_service.configuration.security.otp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@Validated
@ConfigurationProperties("kanwise.otp")
public record OtpConfigurationProperties(
        @NotNull(message = "OTP_EXPIRATION_NOT_NULL") Duration expiration,
        @NotNull(message = "OTP_LENGTH_NOT_NULL") @Min(value = 4) Integer length) {
}
