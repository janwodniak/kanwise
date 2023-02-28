package com.kanwise.kanwise_service.error.custom.member;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException() {
        super("MEMBER_NOT_FOUND");
    }
}
