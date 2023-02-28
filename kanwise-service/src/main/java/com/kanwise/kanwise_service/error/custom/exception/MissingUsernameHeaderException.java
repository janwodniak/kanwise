package com.kanwise.kanwise_service.error.custom.exception;

public class MissingUsernameHeaderException extends RuntimeException {

    public MissingUsernameHeaderException() {
        super("USERNAME_HEADER_IS_MISSING");
    }
}
