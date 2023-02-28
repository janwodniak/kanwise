package com.kanwise.report_service.configuration.spaces;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;


@Validated
@ConfigurationProperties("kanwise.digitalocean.spaces")
public record SpacesConfigurationProperties(
        @NotEmpty(message = "ACCESS_KEY_NOT_EMPTY") String accessKey,
        @NotEmpty(message = "SECRET_KEY_NOT_EMPTY") String secretKey,
        @NotEmpty(message = "SERVICE_ENDPOINT_NOT_EMPTY") String serviceEndpoint,
        @NotEmpty(message = "SINGING_REGION_NOT_EMPTY") String signingRegion) {
}
