package com.kanwise.kanwise_service.error.custom.exception;

public class MissingRoleHeaderException extends RuntimeException {

    public MissingRoleHeaderException() {
        super("ROLE_HEADER_IS_MISSING");
    }
}
