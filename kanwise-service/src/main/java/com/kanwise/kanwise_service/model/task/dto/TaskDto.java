package com.kanwise.kanwise_service.model.task.dto;

import com.kanwise.kanwise_service.model.task.TaskPriority;
import com.kanwise.kanwise_service.model.task.TaskType;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

import java.time.Duration;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class TaskDto extends RepresentationModel<TaskDto> {
    long taskId;
    long projectId;
    String authorUsername;
    int assignedMembersCount;
    int commentsCount;
    int statusesCount;
    String title;
    String description;
    TaskPriority priority;
    TaskType type;
    Duration estimatedTime;
    TaskStatusLabel currentStatus;
}






