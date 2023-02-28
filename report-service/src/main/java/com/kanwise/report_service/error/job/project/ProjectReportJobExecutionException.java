package com.kanwise.report_service.error.job.project;

public class ProjectReportJobExecutionException extends RuntimeException {
    public ProjectReportJobExecutionException(String message) {
        super("PROJECT_REPORT_JOB_EXECUTION_FAILED_FOR_JOB_WITH_ID_%s".formatted(message));
    }
}
