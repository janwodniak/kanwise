package com.kanwise.report_service.configuration.pdf;

import com.kanwise.report_service.model.report.ReportType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "pdf.generated")
public record PdfConfigurationProperties(Map<ReportType, String> paths) {

    public String getGeneratedDirectory(ReportType reportType) {
        return paths.get(reportType);
    }
}
