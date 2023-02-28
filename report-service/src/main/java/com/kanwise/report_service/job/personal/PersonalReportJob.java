package com.kanwise.report_service.job.personal;

import com.kanwise.report_service.job.common.JobIdentityResolver;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.service.job.common.JobService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PersonalReportJob implements Job, JobIdentityResolver {

    private final JobService<PersonalReportJobInformation> personalReportJobService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        personalReportJobService.executeJob(resolveJobId(jobExecutionContext));
    }
}
