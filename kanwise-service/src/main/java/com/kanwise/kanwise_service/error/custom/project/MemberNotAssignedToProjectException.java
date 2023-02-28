package com.kanwise.kanwise_service.error.custom.project;

public class MemberNotAssignedToProjectException extends RuntimeException {
    public MemberNotAssignedToProjectException(String username, long projectId) {
        super("MEMBER_WITH_USERNAME_%s_IS_NOT_ASSIGNED_TO_PROJECT_WITH_ID_%d".formatted(username, projectId));
    }
}
