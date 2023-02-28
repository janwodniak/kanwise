package com.kanwise.notification_service.configuration.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kanwise.directory")
public record DirectoryConfigurationProperties(String emailTemplates) {
}