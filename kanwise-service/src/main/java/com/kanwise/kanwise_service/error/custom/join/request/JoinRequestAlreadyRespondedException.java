package com.kanwise.kanwise_service.error.custom.join.request;

public class JoinRequestAlreadyRespondedException extends RuntimeException {
    public JoinRequestAlreadyRespondedException() {
        super("JOIN_REQUEST_ALREADY_RESPONDED");
    }
}
