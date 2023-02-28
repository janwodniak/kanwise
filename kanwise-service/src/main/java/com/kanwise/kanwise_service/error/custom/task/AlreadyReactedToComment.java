package com.kanwise.kanwise_service.error.custom.task;

public class AlreadyReactedToComment extends RuntimeException {
    public AlreadyReactedToComment() {
        super("ALREADY_REACTED_TO_COMMENT");
    }
}
