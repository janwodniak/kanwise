package com.kanwise.kanwise_service.error.custom.task;

public class MemberAlreadyAssignedToTaskException extends RuntimeException {
    public MemberAlreadyAssignedToTaskException(String username, long taskId) {
        super("MEMBER_WITH_USERNAME_%s_IS_ALREADY_ASSIGNED_TO_TASK_WITH_ID_%d".formatted(username, taskId));
    }
}
