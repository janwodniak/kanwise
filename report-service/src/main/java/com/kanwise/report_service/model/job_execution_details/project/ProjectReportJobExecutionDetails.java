package com.kanwise.report_service.model.job_execution_details.project;

import com.kanwise.report_service.model.job_execution_details.common.JobExecutionDetails;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ProjectReportJobExecutionDetails extends JobExecutionDetails<ProjectReportJobInformation> {
    private String reportUrl;
}
