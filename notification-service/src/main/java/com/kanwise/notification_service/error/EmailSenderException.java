package com.kanwise.notification_service.error;

public class EmailSenderException extends RuntimeException {
    public EmailSenderException(String message) {
        super("FAILED TO SEND EMAIL: " + message);
    }
}
