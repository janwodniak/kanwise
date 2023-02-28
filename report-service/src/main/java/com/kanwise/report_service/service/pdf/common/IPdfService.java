package com.kanwise.report_service.service.pdf.common;

import com.kanwise.report_service.model.report.ReportType;

import java.util.Map;

public interface IPdfService {

    void generatePdf(ReportType reportType, Map<String, Object> data, String fileName);
}
