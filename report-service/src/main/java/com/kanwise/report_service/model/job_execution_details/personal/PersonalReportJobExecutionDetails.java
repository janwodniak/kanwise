package com.kanwise.report_service.model.job_execution_details.personal;

import com.kanwise.report_service.model.job_execution_details.common.JobExecutionDetails;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class PersonalReportJobExecutionDetails extends JobExecutionDetails<PersonalReportJobInformation> {
    private String reportUrl;
}
