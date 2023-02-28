package com.kanwise.report_service.error.job.common;

public class JobResumingException extends RuntimeException {
    public JobResumingException(String id) {
        super("JOB_WITH_ID_%s_RESUMING_FAILED".formatted(id));
    }
}
