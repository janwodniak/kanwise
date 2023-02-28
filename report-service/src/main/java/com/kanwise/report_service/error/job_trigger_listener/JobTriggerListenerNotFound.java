package com.kanwise.report_service.error.job_trigger_listener;


public class JobTriggerListenerNotFound extends RuntimeException {

    public JobTriggerListenerNotFound(String clazz) {
        super("JOB_TRIGGER_LISTENER_NOT_FOUND_FOR_%s_CLASS".formatted(clazz));
    }
}
