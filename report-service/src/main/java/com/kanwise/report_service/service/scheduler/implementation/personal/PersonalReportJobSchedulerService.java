package com.kanwise.report_service.service.scheduler.implementation.personal;

import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.service.job_information.common.JobInformationService;
import com.kanwise.report_service.service.scheduler.implementation.GenericJobSchedulerService;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class PersonalReportJobSchedulerService extends GenericJobSchedulerService<PersonalReportJobInformation> {

    public PersonalReportJobSchedulerService(Scheduler scheduler, ApplicationContext applicationContext, JobInformationService<PersonalReportJobInformation> personalReportJobInformationService) {
        super(scheduler, applicationContext, personalReportJobInformationService);
    }
}
