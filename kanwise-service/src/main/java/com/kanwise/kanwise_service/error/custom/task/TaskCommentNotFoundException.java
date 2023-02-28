package com.kanwise.kanwise_service.error.custom.task;

public class TaskCommentNotFoundException extends RuntimeException {
    public TaskCommentNotFoundException() {
        super("TASK_COMMENT_NOT_FOUND");
    }
}
