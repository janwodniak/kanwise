package com.kanwise.report_service.service.report_data.common;

import com.kanwise.report_service.model.job_information.common.JobInformation;

import java.util.Map;

public interface ReportDataService<T extends JobInformation> {

    Map<String, Object> getReportData(T jobInformation);
}
