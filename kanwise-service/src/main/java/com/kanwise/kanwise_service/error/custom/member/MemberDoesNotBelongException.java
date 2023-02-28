package com.kanwise.kanwise_service.error.custom.member;

public class MemberDoesNotBelongException extends RuntimeException {
    public MemberDoesNotBelongException(String message) {
        super(message);
    }
}
