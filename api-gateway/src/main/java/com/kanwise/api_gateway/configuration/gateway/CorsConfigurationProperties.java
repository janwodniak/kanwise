package com.kanwise.api_gateway.configuration.gateway;

import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Builder
@Validated
@ConfigurationProperties("kanwise.cors")
public record CorsConfigurationProperties(
        List<String> allowedOrigins
) {
}
