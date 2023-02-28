package com.kanwise.report_service.error.exception;

public class MissingUsernameHeaderException extends RuntimeException {

    public MissingUsernameHeaderException() {
        super("USERNAME_HEADER_IS_MISSING");
    }
}
