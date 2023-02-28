package com.kanwise.notification_service.service.email.template.implementation;

import com.kanwise.notification_service.configuration.email.EmailTemplateNamesConfigurationProperties;
import com.kanwise.notification_service.model.email.EmailMessageType;
import com.kanwise.notification_service.service.email.template.IHtmlTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class HtmlTemplateService implements IHtmlTemplateService {

    private final EmailTemplateNamesConfigurationProperties emailTemplateConfigurationProperties;
    private final TemplateEngine templateEngine;

    @Override
    public String generateHtmlTemplate(Map<String, Object> valuesMap, EmailMessageType emailMessageType) {
        String templateName = emailTemplateConfigurationProperties.names().get(emailMessageType);
        Context context = new Context();
        context.setVariables(valuesMap);
        return templateEngine.process(templateName, context);
    }
}
