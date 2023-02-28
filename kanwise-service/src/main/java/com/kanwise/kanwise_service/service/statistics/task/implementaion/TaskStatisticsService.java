package com.kanwise.kanwise_service.service.statistics.task.implementaion;

import com.kanwise.kanwise_service.error.custom.task.TaskNotFoundException;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import com.kanwise.kanwise_service.repository.task.TaskRepository;
import com.kanwise.kanwise_service.service.statistics.TaskStatisticsCalculator;
import com.kanwise.kanwise_service.service.statistics.task.ITaskStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.EnumMap;

import static com.kanwise.kanwise_service.model.task_status.TaskStatusLabel.values;
import static java.util.stream.Stream.of;

@RequiredArgsConstructor
@Service
public class TaskStatisticsService implements ITaskStatisticsService, TaskStatisticsCalculator {

    private final TaskRepository taskRepository;
    private final Clock clock;


    @Transactional(readOnly = true)
    @Override
    public TaskStatistics getTaskStatistics(long id) {
        return taskRepository.findById(id)
                .map(this::getTaskStatistics)
                .orElseThrow(TaskNotFoundException::new);
    }

    public TaskStatistics getTaskStatistics(Task task) {
        return TaskStatistics.builder()
                .task(task)
                .estimatedTime(task.getEstimatedTime())
                .taskStatusDurationMap(findTaskStatusDurationMap(task))
                .build();
    }

    private EnumMap<TaskStatusLabel, Duration> findTaskStatusDurationMap(Task task) {
        return of(values()).collect(() -> new EnumMap<>(TaskStatusLabel.class),
                (map, label) -> map.put(label, task.getSummaryTimeForTaskStatusLabel(label, clock)),
                EnumMap::putAll);
    }
}
