package com.kanwise.report_service.service.scheduler.common;

import com.kanwise.report_service.model.job_information.common.JobInformation;
import org.quartz.Job;

import java.util.List;


public interface JobSchedulerService<T extends JobInformation> {
    T schedule(Class<? extends Job> jobClass, T jobInformation, String group);

    T pauseJob(T jobInformation, String group);

    T resumeJob(T jobInformation, String group);

    List<T> getAllRunningJobsInGroup(String group);

    T getRunningJob(String name, String group);

    void deleteJob(String name, String group);
}
