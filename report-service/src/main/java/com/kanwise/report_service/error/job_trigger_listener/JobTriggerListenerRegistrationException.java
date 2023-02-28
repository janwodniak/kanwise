package com.kanwise.report_service.error.job_trigger_listener;

public class JobTriggerListenerRegistrationException extends RuntimeException {
    public JobTriggerListenerRegistrationException(String clazz) {
        super("JOB_TRIGGER_LISTENER_REGISTRATION_ERROR_FOR_%s_CLASS".formatted(clazz));
    }
}
