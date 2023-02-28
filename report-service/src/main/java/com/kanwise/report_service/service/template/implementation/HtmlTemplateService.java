package com.kanwise.report_service.service.template.implementation;

import com.kanwise.report_service.configuration.pdf.ReportTemplateConfigurationProperties;
import com.kanwise.report_service.model.report.ReportType;
import com.kanwise.report_service.service.template.common.IHtmlTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class HtmlTemplateService implements IHtmlTemplateService {

    private final ReportTemplateConfigurationProperties reportTemplateConfigurationProperties;
    private final TemplateEngine templateEngine;

    @Override
    public String generateHtml(Map<String, Object> data, ReportType reportType) {
        String templateName = reportTemplateConfigurationProperties.getTemplateName(reportType);
        Context context = new Context();
        context.setVariables(data);
        return templateEngine.process(templateName, context);
    }
}
