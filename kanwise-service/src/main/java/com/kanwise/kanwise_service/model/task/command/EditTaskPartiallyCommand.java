package com.kanwise.kanwise_service.model.task.command;

import com.kanwise.kanwise_service.model.task.TaskPriority;
import com.kanwise.kanwise_service.model.task.TaskType;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import com.kanwise.kanwise_service.validation.annotation.common.NullOrNotBlank;
import com.kanwise.kanwise_service.validation.annotation.common.ValueOfEnum;
import lombok.Builder;

import java.time.Duration;

@Builder
public record EditTaskPartiallyCommand(

        @NullOrNotBlank(message = "TITLE_NULL_OR_NOT_BLANK")
        String title,
        @NullOrNotBlank(message = "DESCRIPTION_NULL_OR_NOT_BLANK")
        String description,
        Duration estimatedTime,

        @ValueOfEnum(enumClass = TaskStatusLabel.class)
        String currentStatus,
        @ValueOfEnum(enumClass = TaskPriority.class)
        String priority,
        @ValueOfEnum(enumClass = TaskType.class)
        String type) {
}