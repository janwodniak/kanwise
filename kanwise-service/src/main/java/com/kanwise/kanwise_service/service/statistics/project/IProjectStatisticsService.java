package com.kanwise.kanwise_service.service.statistics.project;

import com.kanwise.kanwise_service.model.project_statistics.ProjectStatistics;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;

import java.util.Set;

public interface IProjectStatisticsService {
    Set<TaskStatistics> findProjectTasksStatistics(long id);

    Set<TaskStatistics> findProjectTasksStatisticsForMember(long id, String username);

    ProjectStatistics getProjectStatisticsForMember(long id, String username);

    ProjectStatistics getProjectStatistics(long id);
}
