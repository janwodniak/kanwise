package com.kanwise.kanwise_service.service.statistics;

import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task.TaskType;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static java.time.Duration.ZERO;
import static java.util.Map.Entry;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public interface TaskStatisticsCalculator {

    default Duration getTotalEstimatedTime(Set<Task> tasks) {
        return tasks.stream()
                .map(Task::getEstimatedTime)
                .reduce(ZERO, Duration::plus);
    }

    default Map<TaskStatusLabel, Duration> getTasksDurationsForTaskStatusLabels(Set<TaskStatistics> taskStatistics) {
        return taskStatistics.stream()
                .map(TaskStatistics::getTaskStatusDurationMap)
                .collect(() -> new EnumMap<>(TaskStatusLabel.class),
                        (map, taskStatusDurationMap) -> taskStatusDurationMap.forEach((key, value) -> map.merge(key, value, Duration::plus)),
                        EnumMap::putAll);
    }

    default EnumMap<TaskStatusLabel, Long> getTasksCountForTaskStatusLabel(Set<Task> tasks) {
        return tasks.stream()
                .collect(() -> new EnumMap<>(TaskStatusLabel.class),
                        (map, task) -> map.put(task.getCurrentStatus(),
                                map.getOrDefault(task.getCurrentStatus(), 0L) + 1),
                        EnumMap::putAll);
    }

    default EnumMap<TaskType, Long> getTasksCountForTaskType(Set<Task> tasks) {
        return tasks.stream()
                .collect(() -> new EnumMap<>(TaskType.class),
                        (map, task) -> map.put(task.getType(),
                                map.getOrDefault(task.getType(), 0L) + 1),
                        EnumMap::putAll);
    }

    default Map<String, EnumMap<TaskStatusLabel, Long>> getTasksCountForProjectAndTaskStatusLabel(Set<Project> projects) {
        return projects.stream()
                .map(Project::getTasks)
                .flatMap(Set::stream)
                .collect(groupingBy(task -> task.getProject().getTitle(), toSet()))
                .entrySet()
                .stream()
                .collect(toMap(Entry::getKey, entry -> getTasksCountForTaskStatusLabel(entry.getValue())));
    }

    default long getPerformancePercentage(Duration estimatedTime, Duration realTime) {
        if (realTime != null && estimatedTime != null && estimatedTime.compareTo(ZERO) != 0 && realTime.compareTo(ZERO) != 0) {
            long round = Math.round((((double) estimatedTime.toMillis()) / realTime.toMillis()) * 100);
            if (round > 1000) {
                return 1000;
            } else {
                return round;
            }
        } else {
            return 0;
        }
    }
}
