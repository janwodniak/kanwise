package com.kanwise.kanwise_service.service.statistics.project.implementation;

import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.project_statistics.ProjectStatistics;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task.TaskType;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;
import com.kanwise.kanwise_service.service.project.IProjectService;
import com.kanwise.kanwise_service.service.statistics.TaskStatisticsCalculator;
import com.kanwise.kanwise_service.service.statistics.project.IProjectStatisticsService;
import com.kanwise.kanwise_service.service.statistics.task.ITaskStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static com.kanwise.kanwise_service.model.task_status.TaskStatusLabel.IN_PROGRESS;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectStatisticsService implements IProjectStatisticsService, TaskStatisticsCalculator {

    private final IProjectService projectService;
    private final ITaskStatisticsService taskStatisticsService;


    @Override
    public Set<TaskStatistics> findProjectTasksStatistics(long id) {
        return projectService.findProjectById(id).getTasks()
                .stream()
                .map(taskStatisticsService::getTaskStatistics)
                .collect(toSet());
    }

    @Override
    public Set<TaskStatistics> findProjectTasksStatisticsForMember(long id, String username) {
        return projectService.findProjectTasksForMember(id, username)
                .stream()
                .map(taskStatisticsService::getTaskStatistics)
                .collect(toSet());
    }

    @Override
    public ProjectStatistics getProjectStatisticsForMember(long id, String username) {
        Project project = projectService.findProjectById(id);
        Set<Task> projectTasks = projectService.findProjectTasksForMember(id, username);
        return getProjectStatistics(project, projectTasks);
    }

    private ProjectStatistics getProjectStatistics(Project project, Set<Task> projectTasks) {
        Set<TaskStatistics> taskStatistics = projectTasks.stream()
                .map(taskStatisticsService::getTaskStatistics)
                .collect(toSet());

        return ProjectStatistics.builder()
                .project(project)
                .totalEstimatedTime(getTotalEstimatedTime(projectTasks))
                .totalTasksStatusDurationMap(getTasksDurationsForTaskStatusLabels(taskStatistics))
                .totalTasksStatusCountMap(getTasksCountForTaskStatusLabel(projectTasks))
                .performancePercentage(getPerformancePercentage(getTotalEstimatedTime(projectTasks), getTasksDurationsForTaskStatusLabels(taskStatistics).get(IN_PROGRESS)))
                .totalTasksTypeCountMap(getTasksCountForTaskTypeLabel(projectTasks))
                .build();
    }

    @Override
    public ProjectStatistics getProjectStatistics(long id) {
        Project project = projectService.findProjectById(id);
        Set<Task> projectTasks = projectService.findProjectTasks(id);
        return getProjectStatistics(project, projectTasks);
    }


    private Map<TaskType, Long> getTasksCountForTaskTypeLabel(Set<Task> projectTasks) {
        return projectTasks.stream()
                .collect(groupingBy(Task::getType, counting()));
    }
}

