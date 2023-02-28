package com.kanwise.kanwise_service.model.task.command;

import com.kanwise.kanwise_service.model.task.TaskPriority;
import com.kanwise.kanwise_service.model.task.TaskType;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import com.kanwise.kanwise_service.validation.annotation.common.ValueOfEnum;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@Builder
public record EditTaskCommand(
        @NotBlank(message = "TITLE_NOT_BLANK")
        String title,
        @NotBlank(message = "DESCRIPTION_NOT_BLANK")
        String description,
        @NotNull(message = "ESTIMATED_TIME_NOT_NULL")
        Duration estimatedTime,
        @ValueOfEnum(enumClass = TaskStatusLabel.class)
        @NotNull(message = "CURRENT_STATUS_NOT_NULL")
        String currentStatus,
        @ValueOfEnum(enumClass = TaskPriority.class)
        @NotNull(message = "PRIORITY_NOT_NULL")
        String priority,
        @ValueOfEnum(enumClass = TaskType.class)
        @NotNull(message = "TYPE_NOT_NULL")
        String type) {
}
