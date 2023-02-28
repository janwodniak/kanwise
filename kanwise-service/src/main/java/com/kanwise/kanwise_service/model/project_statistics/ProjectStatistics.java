package com.kanwise.kanwise_service.model.project_statistics;

import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.task.TaskType;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProjectStatistics {
    private Project project;
    private Duration totalEstimatedTime;
    private Duration totalExistenceTime;
    private Map<TaskStatusLabel, Duration> totalTasksStatusDurationMap;
    private Map<TaskStatusLabel, Long> totalTasksStatusCountMap;
    private long performancePercentage;
    private Map<TaskType, Long> totalTasksTypeCountMap;
}
