package com.kanwise.report_service.service.template.common;


import com.kanwise.report_service.model.report.ReportType;

import java.util.Map;

public interface IHtmlTemplateService {
    String generateHtml(Map<String, Object> data, ReportType reportType);
}
