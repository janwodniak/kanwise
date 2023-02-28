package com.kanwise.report_service.service.job_executor.common;

import com.kanwise.report_service.model.job_execution_details.common.JobExecutionDetails;
import com.kanwise.report_service.model.job_information.common.JobInformation;

import java.util.concurrent.CompletableFuture;

public interface JobExecutorService<R extends JobExecutionDetails<T>, T extends JobInformation> {

    CompletableFuture<R> execute(T jobInformation);
}
