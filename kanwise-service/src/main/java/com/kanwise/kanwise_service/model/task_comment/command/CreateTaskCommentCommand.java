package com.kanwise.kanwise_service.model.task_comment.command;

import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Builder
public record CreateTaskCommentCommand(
        @NotBlank(message = "AUTHOR_USERNAME_NOT_BLANK")
        String authorUsername,
        @NotNull(message = "TASK_ID_NOT_NULL")
        Long taskId,
        @NotBlank(message = "CONTENT_NOT_BLANK")
        String content) {
}
