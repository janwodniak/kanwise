package com.kanwise.kanwise_service.error.custom.task;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException() {
        super("TASK_NOT_FOUND");
    }
}
