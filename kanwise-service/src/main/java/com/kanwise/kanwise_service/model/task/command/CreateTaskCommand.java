package com.kanwise.kanwise_service.model.task.command;

import com.kanwise.kanwise_service.model.task.TaskPriority;
import com.kanwise.kanwise_service.model.task.TaskType;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import com.kanwise.kanwise_service.validation.annotation.common.ValueOfEnum;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Set;

@Builder
public record CreateTaskCommand(
        @NotBlank(message = "TITLE_NOT_BLANK")
        String title,
        @NotBlank(message = "DESCRIPTION_NOT_BLANK")
        String description,
        @NotNull(message = "ESTIMATED_TIME_NOT_NULL")
        Duration estimatedTime,
        @NotBlank(message = "AUTHOR_USERNAME_NOT_BLANK")
        String authorUsername,
        @NotNull(message = "PRIORITY_NOT_NULL")
        @ValueOfEnum(enumClass = TaskPriority.class)
        String priority,
        @NotNull(message = "TYPE_NOT_NULL")
        @ValueOfEnum(enumClass = TaskType.class)
        String type,
        @NotNull(message = "PROJECT_ID_NOT_NULL")
        Long projectId,
        @NotNull(message = "MEMBERS_USERNAMES_NOT_NULL")
        Set<String> membersUsernames,
        @NotNull(message = "CURRENT_STATUS_NOT_NULL")
        @ValueOfEnum(enumClass = TaskStatusLabel.class)
        String currentStatus) {
}
