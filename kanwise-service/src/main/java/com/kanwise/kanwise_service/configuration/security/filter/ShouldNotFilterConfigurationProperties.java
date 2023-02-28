package com.kanwise.kanwise_service.configuration.security.filter;

import com.kanwise.kanwise_service.model.http.EndpointSpecification;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "kanwise.filter.ommited")
public record ShouldNotFilterConfigurationProperties(
        List<EndpointSpecification> authentication
) {
}
