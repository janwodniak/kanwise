package com.kanwise.report_service.service.job_information.common;

import com.kanwise.report_service.model.job_information.common.JobInformation;

import java.util.List;


public interface JobInformationService<T extends JobInformation> {

    T saveJobInformation(T jobInformation);

    T getJobInformation(String id);

    List<T> getAllJobInformation();

    void deleteJobInformation(String id);

    T updateJobInformation(T jobInformation);
}
