package com.kanwise.report_service.service.job.implementation.project;

import com.kanwise.report_service.error.job.project.ProjectReportJobExecutionException;
import com.kanwise.report_service.job.project.ProjectReportJob;
import com.kanwise.report_service.model.job_execution_details.project.ProjectReportJobExecutionDetails;
import com.kanwise.report_service.model.job_information.common.JobStatus;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.model.monitoring.project.ProjectReportJobLog;
import com.kanwise.report_service.service.job.common.JobService;
import com.kanwise.report_service.service.job_executor.common.JobExecutorService;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import com.kanwise.report_service.service.job_information.monitoring.common.MonitoringService;
import com.kanwise.report_service.service.scheduler.implementation.GenericJobSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.kanwise.report_service.constant.job.JobLogConstant.JOB_CREATED_MESSAGE;
import static com.kanwise.report_service.constant.job.JobLogConstant.JOB_DELETED_MESSAGE;
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
import static com.kanwise.report_service.model.report.JobGroup.PROJECT_REPORT;
import static java.util.Map.of;

@RequiredArgsConstructor
@Service
public class ProjectReportJobService implements JobService<ProjectReportJobInformation> {

    private final GenericJobSchedulerService<ProjectReportJobInformation> schedulerService;
    private final JobInformationService<ProjectReportJobInformation> jobInformationService;
    private final JobExecutorService<ProjectReportJobExecutionDetails, ProjectReportJobInformation> jobExecutorService;
    private final MonitoringService<ProjectReportJobLog, ProjectReportJobInformation> projectReportJobMonitoringService;

    @Override
    public ProjectReportJobInformation runJob(ProjectReportJobInformation jobInformation) {
        ProjectReportJobInformation projectReportJobInformation = jobInformationService.saveJobInformation(jobInformation);
        projectReportJobMonitoringService.log(CREATED, JOB_CREATED_MESSAGE, projectReportJobInformation);
        return schedulerService.schedule(ProjectReportJob.class, projectReportJobInformation, PROJECT_REPORT.name());
    }

    @Override
    public ProjectReportJobInformation stopJob(String id) {
        ProjectReportJobInformation projectReportJobInformation = getJob(id);
        projectReportJobInformation.setStatus(JobStatus.STOPPED);
        jobInformationService.updateJobInformation(projectReportJobInformation);
        projectReportJobMonitoringService.log(STOPPED, JOB_STOPPED_MESSAGE, projectReportJobInformation);
        schedulerService.pauseJob(projectReportJobInformation, PROJECT_REPORT.name());
        return projectReportJobInformation;
    }

    @Override
    public ProjectReportJobInformation restartJob(String id) {
        ProjectReportJobInformation projectReportJobInformation = getJob(id);
        projectReportJobInformation.setStatus(JobStatus.RESTARTED);
        jobInformationService.saveJobInformation(projectReportJobInformation);
        projectReportJobMonitoringService.log(RESTARTED, JOB_RESTARTED_MESSAGE, projectReportJobInformation);
        schedulerService.resumeJob(projectReportJobInformation, PROJECT_REPORT.name());
        return projectReportJobInformation;
    }

    @Override
    public void deleteJob(String id) {
        projectReportJobMonitoringService.log(DELETED, JOB_DELETED_MESSAGE, jobInformationService.getJobInformation(id));
        schedulerService.deleteJob(id, PROJECT_REPORT.name());
    }

    @Override
    public List<ProjectReportJobInformation> getAllJobs() {
        return new ArrayList<>();
    }

    @Override
    public ProjectReportJobInformation getJob(String id) {
        return schedulerService.getRunningJob(id, PROJECT_REPORT.name());
    }

    @Override
    public void executeJob(String id) {
        ProjectReportJobInformation projectReportJobInformation = jobInformationService.getJobInformation(id);
        jobExecutorService.execute(projectReportJobInformation)
                .thenAccept(handleJobExecutionSuccess(projectReportJobInformation))
                .exceptionally(handleJobExecutionException(projectReportJobInformation));
    }

    private Consumer<ProjectReportJobExecutionDetails> handleJobExecutionSuccess(ProjectReportJobInformation jobInformation) {
        return jobExecutionDetails -> {
            jobInformation.setStatus(RUNNING);
            jobInformationService.updateJobInformation(jobInformation);
            projectReportJobMonitoringService.log(
                    SUCCESS,
                    JOB_EXECUTION_SUCCESS_MESSAGE,
                    jobInformation,
                    of(REPORT_URL, jobExecutionDetails.getReportUrl()));
        };
    }

    private Function<Throwable, Void> handleJobExecutionException(ProjectReportJobInformation jobInformation) {
        return throwable -> {
            jobInformation.setStatus(FAILED);
            jobInformationService.updateJobInformation(jobInformation);
            projectReportJobMonitoringService.log(ERROR, throwable.getMessage(), jobInformation);
            throw new ProjectReportJobExecutionException(jobInformation.getId());
        };
    }
}
