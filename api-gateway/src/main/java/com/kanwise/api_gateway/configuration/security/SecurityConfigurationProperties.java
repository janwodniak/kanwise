package com.kanwise.api_gateway.configuration.security;

import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Builder
@Validated
@ConfigurationProperties("kanwise.security")
public record SecurityConfigurationProperties(
        @NotBlank(message = "AUTHENTICATION_ROUTE_NOT_BLANK") String authenticationRoute,
        List<String> openApiEndpoints
) {
}
