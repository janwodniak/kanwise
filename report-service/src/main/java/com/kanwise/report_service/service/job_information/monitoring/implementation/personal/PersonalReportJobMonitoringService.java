package com.kanwise.report_service.service.job_information.monitoring.implementation.personal;

import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.monitoring.common.LogStatus;
import com.kanwise.report_service.model.monitoring.personal.PersonalReportJobLog;
import com.kanwise.report_service.repository.monitoring.personal.PersonalReportJobLogRepository;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import com.kanwise.report_service.service.job_information.monitoring.common.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;
import java.util.Set;

import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
public class PersonalReportJobMonitoringService implements MonitoringService<PersonalReportJobLog, PersonalReportJobInformation> {

    private final PersonalReportJobLogRepository personalReportJobLogRepository;
    private final JobInformationService<PersonalReportJobInformation> personalReportJobInformationService;
    private final Clock clock;


    @Transactional
    @Override
    public PersonalReportJobLog log(LogStatus status, String message, PersonalReportJobInformation jobInformation) {
        PersonalReportJobLog log = generateLog(status, message);
        jobInformation.addLog(log);
        return personalReportJobLogRepository.save(log);
    }

    @Transactional
    @Override
    public PersonalReportJobLog log(LogStatus status, String message, PersonalReportJobInformation jobInformation, Map<String, String> data) {
        PersonalReportJobLog log = generateLog(status, message);
        log.setData(data);
        jobInformation.addLog(log);
        return personalReportJobLogRepository.save(log);
    }

    @Override
    public Set<PersonalReportJobLog> getLogs(String jobId) {
        return personalReportJobInformationService.getJobInformation(jobId).getLogs();
    }

    private PersonalReportJobLog generateLog(LogStatus status, String message) {
        return PersonalReportJobLog.builder()
                .status(status)
                .message(message)
                .timestamp(now(clock))
                .build();
    }
}
