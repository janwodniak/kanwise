package com.kanwise.notification_service.service.email.template;

import com.kanwise.notification_service.model.email.EmailMessageType;

import java.util.Map;

public interface IHtmlTemplateService {
    String generateHtmlTemplate(Map<String, Object> valuesMap, EmailMessageType emailMessageType);
}
