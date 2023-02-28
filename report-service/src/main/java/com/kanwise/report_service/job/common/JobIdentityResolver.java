package com.kanwise.report_service.job.common;

import org.quartz.JobExecutionContext;

import static com.kanwise.report_service.constant.job.JobConstant.ID;

public interface JobIdentityResolver {

    default String resolveJobId(JobExecutionContext jobExecutionContext) {
        return (String) jobExecutionContext.getJobDetail().getJobDataMap().get(ID);
    }
}
