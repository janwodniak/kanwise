package com.kanwise.notification_service.service.email.template.implementation.mapping;

import com.kanwise.notification_service.model.email.Email;
import com.kanwise.notification_service.model.email.EmailRequest;
import com.kanwise.notification_service.service.email.template.IHtmlTemplateService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class EmailRequestToEmailConverter implements Converter<EmailRequest, Email> {

    private final IHtmlTemplateService htmlTemplateService;

    @Override
    public Email convert(MappingContext<EmailRequest, Email> mappingContext) {
        EmailRequest source = mappingContext.getSource();
        return Email.builder()
                .to(source.getTo())
                .subject(source.getSubject())
                .content(getContent(source))
                .isHtml(source.isHtml())
                .build();
    }

    private String getContent(EmailRequest emailRequest) {
        return emailRequest.isHtml() ? htmlTemplateService.generateHtmlTemplate(emailRequest.getData(), emailRequest.getType()) : emailRequest.getData().values().toString();
    }
}
