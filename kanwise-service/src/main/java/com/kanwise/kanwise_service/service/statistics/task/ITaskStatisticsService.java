package com.kanwise.kanwise_service.service.statistics.task;

import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;

public interface ITaskStatisticsService {
    TaskStatistics getTaskStatistics(long id);

    TaskStatistics getTaskStatistics(Task task);
}
