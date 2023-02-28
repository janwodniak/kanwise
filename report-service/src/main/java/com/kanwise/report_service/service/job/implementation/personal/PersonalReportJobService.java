package com.kanwise.report_service.service.job.implementation.personal;

import com.kanwise.report_service.error.job.personal.PersonalReportJobExecutionException;
import com.kanwise.report_service.job.personal.PersonalReportJob;
import com.kanwise.report_service.model.job_execution_details.personal.PersonalReportJobExecutionDetails;
import com.kanwise.report_service.model.job_information.common.JobStatus;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.monitoring.personal.PersonalReportJobLog;
import com.kanwise.report_service.service.job.common.JobService;
import com.kanwise.report_service.service.job_executor.common.JobExecutorService;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import com.kanwise.report_service.service.job_information.monitoring.common.MonitoringService;
import com.kanwise.report_service.service.scheduler.implementation.GenericJobSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.kanwise.report_service.constant.job.JobLogConstant.JOB_CREATED_MESSAGE;
import static com.kanwise.report_service.constant.job.JobLogConstant.JOB_DELETED_MESSAGE;
import static com.kanwise.report_service.constant.job.JobLogConstant.JOB_EXECUTION_FAILED_MESSAGE;
import static com.kanwise.report_service.constant.job.JobLogConstant.JOB_EXECUTION_SUCCESS_MESSAGE;
import static com.kanwise.report_service.constant.job.JobLogConstant.JOB_RESTARTED_MESSAGE;
import static com.kanwise.report_service.constant.job.JobLogConstant.JOB_STOPPED_MESSAGE;
import static com.kanwise.report_service.constant.job.JobLogConstant.REPORT_URL;
import static com.kanwise.report_service.model.job_information.common.JobStatus.FAILED;
import static com.kanwise.report_service.model.job_information.common.JobStatus.RUNNING;
import static com.kanwise.report_service.model.monitoring.common.LogStatus.CREATED;
import static com.kanwise.report_service.model.monitoring.common.LogStatus.DELETED;
import static com.kanwise.report_service.model.monitoring.common.LogStatus.ERROR;
import static com.kanwise.report_service.model.monitoring.common.LogStatus.RESTARTED;
import static com.kanwise.report_service.model.monitoring.common.LogStatus.STOPPED;
import static com.kanwise.report_service.model.monitoring.common.LogStatus.SUCCESS;
import static com.kanwise.report_service.model.report.JobGroup.PERSONAL_REPORT;
import static java.util.Map.of;

@RequiredArgsConstructor
@Service
public class PersonalReportJobService implements JobService<PersonalReportJobInformation> {


    private final GenericJobSchedulerService<PersonalReportJobInformation> schedulerService;
    private final JobExecutorService<PersonalReportJobExecutionDetails, PersonalReportJobInformation> jobExecutorService;
    private final JobInformationService<PersonalReportJobInformation> jobInformationService;
    private final MonitoringService<PersonalReportJobLog, PersonalReportJobInformation> personalReportJobMonitoringService;


    @Override
    public PersonalReportJobInformation runJob(PersonalReportJobInformation jobInformation) {
        PersonalReportJobInformation personalReportJobInformation = jobInformationService.saveJobInformation(jobInformation);
        personalReportJobMonitoringService.log(CREATED, JOB_CREATED_MESSAGE, personalReportJobInformation);
        return schedulerService.schedule(PersonalReportJob.class, personalReportJobInformation, PERSONAL_REPORT.name());
    }

    @Override
    public PersonalReportJobInformation stopJob(String id) {
        PersonalReportJobInformation personalReportJobInformation = getJob(id);
        personalReportJobInformation.setStatus(JobStatus.STOPPED);
        jobInformationService.updateJobInformation(personalReportJobInformation);
        personalReportJobMonitoringService.log(STOPPED, JOB_STOPPED_MESSAGE, personalReportJobInformation);
        schedulerService.pauseJob(personalReportJobInformation, PERSONAL_REPORT.name());
        return personalReportJobInformation;
    }

    @Override
    public PersonalReportJobInformation restartJob(String id) {
        PersonalReportJobInformation personalReportJobInformation = getJob(id);
        personalReportJobInformation.setStatus(JobStatus.RESTARTED);
        jobInformationService.updateJobInformation(personalReportJobInformation);
        personalReportJobMonitoringService.log(RESTARTED, JOB_RESTARTED_MESSAGE, personalReportJobInformation);
        schedulerService.resumeJob(personalReportJobInformation, PERSONAL_REPORT.name());
        return personalReportJobInformation;
    }

    @Override
    public void deleteJob(String id) {
        personalReportJobMonitoringService.log(DELETED, JOB_DELETED_MESSAGE, jobInformationService.getJobInformation(id));
        schedulerService.deleteJob(id, PERSONAL_REPORT.name());
    }

    @Override
    public List<PersonalReportJobInformation> getAllJobs() {
        return jobInformationService.getAllJobInformation();
    }

    @Override
    public PersonalReportJobInformation getJob(String id) {
        return jobInformationService.getJobInformation(id);
    }

    @Override
    public void executeJob(String id) {
        PersonalReportJobInformation personalReportJobInformation = jobInformationService.getJobInformation(id);
        jobExecutorService.execute(personalReportJobInformation)
                .thenAccept(handleJobExecutionSuccess(personalReportJobInformation))
                .exceptionally(handleJobExecutionException(personalReportJobInformation));
    }

    private Consumer<PersonalReportJobExecutionDetails> handleJobExecutionSuccess(PersonalReportJobInformation jobInformation) {
        return executionDetails -> {
            jobInformation.setStatus(RUNNING);
            jobInformationService.updateJobInformation(jobInformation);
            personalReportJobMonitoringService.log(
                    SUCCESS,
                    JOB_EXECUTION_SUCCESS_MESSAGE,
                    jobInformation,
                    of(REPORT_URL, executionDetails.getReportUrl()));
        };
    }

    private Function<Throwable, Void> handleJobExecutionException(PersonalReportJobInformation jobInformation) {
        return throwable -> {
            jobInformation.setStatus(FAILED);
            jobInformationService.updateJobInformation(jobInformation);
            personalReportJobMonitoringService.log(ERROR, JOB_EXECUTION_FAILED_MESSAGE.formatted(throwable.getMessage()), jobInformation);
            throw new PersonalReportJobExecutionException(jobInformation.getId());
        };
    }
}