package com.kanwise.report_service.error.job.common;

public class JobPausingException extends RuntimeException {
    public JobPausingException(String id) {
        super("JOB_WITH_ID_%s_PAUSING_FAILED".formatted(id));
    }
}

