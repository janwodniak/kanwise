package com.kanwise.kanwise_service.error.custom.member;

public class MemberAlreadyAssignedToProjectException extends RuntimeException {
    public MemberAlreadyAssignedToProjectException(String username, long projectId) {
        super("MEMBER_WITH_USERNAME_%s_IS_ALREADY_ASSIGNED_TO_PROJECT_WITH_ID_%d".formatted(username, projectId));
    }
}
