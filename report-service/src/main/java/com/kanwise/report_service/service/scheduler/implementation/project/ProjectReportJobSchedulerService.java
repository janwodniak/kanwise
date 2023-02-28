package com.kanwise.report_service.service.scheduler.implementation.project;

import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import com.kanwise.report_service.service.scheduler.implementation.GenericJobSchedulerService;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ProjectReportJobSchedulerService extends GenericJobSchedulerService<ProjectReportJobInformation> {

    public ProjectReportJobSchedulerService(Scheduler scheduler, ApplicationContext applicationContext, JobInformationService<ProjectReportJobInformation> projectReportJobInformationService) {
        super(scheduler, applicationContext, projectReportJobInformationService);
    }
}
