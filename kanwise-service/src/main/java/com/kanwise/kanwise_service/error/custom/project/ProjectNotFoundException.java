package com.kanwise.kanwise_service.error.custom.project;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException() {
        super("PROJECT_NOT_FOUND");
    }
}
