package com.kanwise.kanwise_service.error.custom.join.response;

public class JoinResponseNotFoundException extends RuntimeException {
    public JoinResponseNotFoundException() {
        super("JOIN_RESPONSE_NOT_FOUND");
    }
}
