package com.kanwise.report_service.error.job.common;

public class JobAlreadyExistsException extends RuntimeException {
    public JobAlreadyExistsException(String id) {
        super("JOB_WITH_ID_%s_ALREADY_EXISTS".formatted(id));
    }
}
