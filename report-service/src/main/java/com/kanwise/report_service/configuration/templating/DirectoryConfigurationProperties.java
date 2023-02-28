package com.kanwise.report_service.configuration.templating;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kanwise.directory")
public record DirectoryConfigurationProperties(String reportTemplates) {
}