package com.kanwise.report_service.listener.project;

import com.kanwise.report_service.listener.common.GenericJobTriggerListener;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import org.springframework.stereotype.Service;

@Service
public class ProjectReportJobTriggerListener extends GenericJobTriggerListener<ProjectReportJobInformation> {
    public ProjectReportJobTriggerListener(JobInformationService<ProjectReportJobInformation> jobInformationService) {
        super(jobInformationService);
    }
}
