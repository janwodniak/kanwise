package com.kanwise.kanwise_service.error.custom.join.request;

public class JoinRequestNotFoundException extends RuntimeException {
    public JoinRequestNotFoundException() {
        super("JOIN_REQUEST_NOT_FOUND");
    }
}
