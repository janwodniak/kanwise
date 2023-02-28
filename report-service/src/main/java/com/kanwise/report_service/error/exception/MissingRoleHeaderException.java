package com.kanwise.report_service.error.exception;

public class MissingRoleHeaderException extends RuntimeException {

    public MissingRoleHeaderException() {
        super("ROLE_HEADER_IS_MISSING");
    }
}
