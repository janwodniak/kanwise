package com.kanwise.report_service.error.subscriber;

public class SubscriberNotFoundException extends RuntimeException {
    public SubscriberNotFoundException(String username) {
        super("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(username));
    }
}
