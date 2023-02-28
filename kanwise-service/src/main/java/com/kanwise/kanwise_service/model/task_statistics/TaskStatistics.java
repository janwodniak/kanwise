package com.kanwise.kanwise_service.model.task_statistics;

import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.util.Map;

import static java.time.Duration.ZERO;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TaskStatistics {

    private Task task;
    private Duration estimatedTime;
    private Duration totalExistenceTime;
    private Map<TaskStatusLabel, Duration> taskStatusDurationMap;

    @Builder
    public TaskStatistics(Task task, Duration estimatedTime, Map<TaskStatusLabel, Duration> taskStatusDurationMap) {
        this.task = task;
        this.estimatedTime = estimatedTime;
        this.taskStatusDurationMap = taskStatusDurationMap;
        this.totalExistenceTime = getTotalExistenceTime();
    }

    public Duration getTotalExistenceTime() {
        return taskStatusDurationMap.values().stream()
                .reduce(ZERO, Duration::plus);
    }

    public Duration getTimeInStatus(TaskStatusLabel taskStatusLabel) {
        return taskStatusDurationMap.getOrDefault(taskStatusLabel, ZERO);
    }
}
