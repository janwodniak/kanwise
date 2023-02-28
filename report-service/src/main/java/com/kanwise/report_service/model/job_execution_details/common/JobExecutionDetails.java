package com.kanwise.report_service.model.job_execution_details.common;

import com.kanwise.report_service.model.job_information.common.JobInformation;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
public class JobExecutionDetails<T extends JobInformation> {
    private T jobInformation;
    private ExecutionStatus executionStatus;
    private LocalDateTime executionTime;
    private String message;
}
