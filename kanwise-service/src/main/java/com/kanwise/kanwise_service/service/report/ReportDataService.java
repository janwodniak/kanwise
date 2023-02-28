package com.kanwise.kanwise_service.service.report;

import com.kanwise.kanwise_service.model.report.ReportData;

public interface ReportDataService<T extends ReportData, R> {

    T getReportData(R request);
}
