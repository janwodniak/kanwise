package com.kanwise.report_service.job.project;

import com.kanwise.report_service.job.common.JobIdentityResolver;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.service.job.common.JobService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProjectReportJob implements Job, JobIdentityResolver {

    private final JobService<ProjectReportJobInformation> projectReportJobService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        projectReportJobService.executeJob(resolveJobId(jobExecutionContext));
    }
}
