package com.kanwise.report_service.listener.personal;


import com.kanwise.report_service.listener.common.GenericJobTriggerListener;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import org.springframework.stereotype.Service;

@Service
public class PersonalReportJobTriggerListener extends GenericJobTriggerListener<PersonalReportJobInformation> {
    public PersonalReportJobTriggerListener(JobInformationService<PersonalReportJobInformation> jobInformationService) {
        super(jobInformationService);
    }
}
