package com.kanwise.report_service.service.job.common;

import com.kanwise.report_service.model.job_information.common.JobInformation;

import java.util.List;

public interface JobService<T extends JobInformation> {
    T runJob(T jobInfo);

    List<T> getAllJobs();

    T getJob(String id);

    T stopJob(String id);

    T restartJob(String id);

    void deleteJob(String id);

    void executeJob(String id);
}
