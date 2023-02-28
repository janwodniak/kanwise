package com.kanwise.report_service.service.job_information.monitoring.implementation.project;

import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.model.monitoring.common.LogStatus;
import com.kanwise.report_service.model.monitoring.project.ProjectReportJobLog;
import com.kanwise.report_service.repository.monitoring.project.ProjectReportJobLogRepository;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import com.kanwise.report_service.service.job_information.monitoring.common.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;
import java.util.Set;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class ProjectReportJobMonitoringService implements MonitoringService<ProjectReportJobLog, ProjectReportJobInformation> {

    private final ProjectReportJobLogRepository projectReportJobLogRepository;

    private final JobInformationService<ProjectReportJobInformation> projectReportJobInformationService;

    private final Clock clock;


    @Transactional
    @Override
    public ProjectReportJobLog log(LogStatus status, String message, ProjectReportJobInformation jobInformation) {
        ProjectReportJobLog log = generateLog(status, message);
        jobInformation.addLog(log);
        return projectReportJobLogRepository.saveAndFlush(log);
    }

    @Transactional
    @Override
    public ProjectReportJobLog log(LogStatus status, String message, ProjectReportJobInformation jobInformation, Map<String, String> data) {
        ProjectReportJobLog log = generateLog(status, message);
        log.setData(data);
        jobInformation.addLog(log);
        return projectReportJobLogRepository.saveAndFlush(log);
    }

    @Override
    public Set<ProjectReportJobLog> getLogs(String jobId) {
        return projectReportJobInformationService.getJobInformation(jobId).getLogs();
    }

    private ProjectReportJobLog generateLog(LogStatus status, String message) {
        return ProjectReportJobLog.builder()
                .status(status)
                .message(message)
                .timestamp(now(clock))
                .build();
    }
}
