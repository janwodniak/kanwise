package com.kanwise.report_service.service.job_information.monitoring.common;

import com.kanwise.report_service.model.job_information.common.JobInformation;
import com.kanwise.report_service.model.monitoring.common.JobLog;
import com.kanwise.report_service.model.monitoring.common.LogStatus;

import java.util.Map;
import java.util.Set;

public interface MonitoringService<T extends JobLog, I extends JobInformation> {
    T log(LogStatus status, String message, I jobInformation);

    T log(LogStatus status, String message, I jobInformation, Map<String, String> data);

    Set<T> getLogs(String jobId);
}
