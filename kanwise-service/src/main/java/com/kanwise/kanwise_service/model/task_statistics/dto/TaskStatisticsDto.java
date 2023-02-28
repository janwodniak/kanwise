package com.kanwise.kanwise_service.model.task_statistics.dto;

import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

import java.time.Duration;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class TaskStatisticsDto extends RepresentationModel<TaskStatisticsDto> {
    long projectId;
    long taskId;
    int assignedMembersCount;
    int commentsCount;
    int statusesCount;
    Duration estimatedTime;
    Duration totalExistenceTime;
    Map<TaskStatusLabel, Duration> taskStatusDurationMap;
}
