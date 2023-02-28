package com.kanwise.report_service.configuration.pdf;

import com.kanwise.report_service.model.report.ReportType;
import com.kanwise.report_service.validation.annotation.template.TemplatePaths;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "pdf.template")
public record ReportTemplateConfigurationProperties(
        @TemplatePaths
        Map<ReportType, String> names
) {
    public String getTemplateName(ReportType reportType) {
        return names.get(reportType);
    }
}
