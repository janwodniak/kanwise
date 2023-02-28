package com.kanwise.notification_service.configuration.email;

import com.kanwise.notification_service.model.email.EmailMessageType;
import com.kanwise.notification_service.validation.annotation.template.TemplatePaths;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "kanwise.email.template")
public record EmailTemplateNamesConfigurationProperties(
        @TemplatePaths
        Map<EmailMessageType, String> names
) {
}


