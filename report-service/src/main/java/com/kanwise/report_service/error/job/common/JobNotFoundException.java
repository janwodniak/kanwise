package com.kanwise.report_service.error.job.common;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String id) {
        super("JOB_WITH_ID_%s_NOT_FOUND".formatted(id));
    }
}
