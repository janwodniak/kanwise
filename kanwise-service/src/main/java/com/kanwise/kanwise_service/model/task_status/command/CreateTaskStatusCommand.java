package com.kanwise.kanwise_service.model.task_status.command;

import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import com.kanwise.kanwise_service.validation.annotation.common.ValueOfEnum;
import lombok.Builder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Builder
public record CreateTaskStatusCommand(
        @NotNull(message = "LABEL_NOT_NULL")
        @ValueOfEnum(enumClass = TaskStatusLabel.class)
        String label,
        @NotNull(message = "TASK_ID_NOT_NULL")
        Long taskId,
        @NotEmpty(message = "USERNAME_NOT_EMPTY")
        String setBy) {
}
