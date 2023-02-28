package com.kanwise.kanwise_service.configuration.security.url;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties("kanwise.url")
public record UrlConfigurationProperties(
        List<String> publicUrls
) {
    public String[] publicUrlsAsArray() {
        return publicUrls.toArray(String[]::new);
    }
}
