package com.kanwise.report_service.error.job.personal;

public class PersonalReportJobExecutionException extends RuntimeException {
    public PersonalReportJobExecutionException(String id) {
        super("PERSONAL_REPORT_JOB_EXECUTION_FAILED_FOR_JOB_WITH_ID_%s".formatted(id));
    }
}
