package com.kanwise.notification_service.configuration.twillio;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties("twilio")
public record TwilioConfigurationProperties(
        String accountSid,
        String authToken,
        String number) {
}
