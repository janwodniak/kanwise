package com.kanwise.kanwise_service.model.member_statistics.dto;

import com.kanwise.kanwise_service.model.task.TaskType;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;


@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class MemberStatisticsDto extends RepresentationModel<MemberStatisticsDto> {
    String memberUsername;
    long totalTasksCount;
    long performancePercentage;
    Duration totalEstimatedTime;
    Map<TaskStatusLabel, Long> totalTasksStatusCountMap;
    Map<TaskStatusLabel, Duration> totalTasksStatusDurationMap;
    Map<TaskType, Long> totalTasksTypeCountMap;
    Map<String, EnumMap<TaskStatusLabel, Long>> totalTasksStatusCountByProjectMap;
}
