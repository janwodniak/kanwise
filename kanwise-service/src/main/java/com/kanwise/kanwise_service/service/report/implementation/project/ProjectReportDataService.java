package com.kanwise.kanwise_service.service.report.implementation.project;

import com.kanwise.clients.report_service.report.model.project.ProjectReportDataRequest;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.report.project.ProjectReportData;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;
import com.kanwise.kanwise_service.service.project.IProjectService;
import com.kanwise.kanwise_service.service.report.ReportDataService;
import com.kanwise.kanwise_service.service.statistics.task.implementaion.TaskStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Service
public class ProjectReportDataService implements ReportDataService<ProjectReportData, ProjectReportDataRequest> {

    private final IProjectService projectService;
    private final TaskStatisticsService taskStatisticsService;

    @Override
    public ProjectReportData getReportData(ProjectReportDataRequest request) {
        Project project = projectService.findProjectById(request.projectId());
        LocalDateTime startDate = request.startDate();
        LocalDateTime endDate = request.endDate();
        List<Task> tasks = getTasksForProjectInTimeRange(project, startDate, endDate);


        return ProjectReportData.builder()
                .startDate(startDate)
                .endDate(endDate)
                .project(project)
                .tasks(generateTaskStatisticsMap(tasks))
                .build();
    }

    private List<Task> getTasksForProjectInTimeRange(Project project, LocalDateTime startDate, LocalDateTime endDate) {
        return projectService.findProjectTasks(project.getId())
                .stream()
                .filter(task -> task.getCreatedAt().isAfter(startDate) && task.getCreatedAt().isBefore(endDate))
                .toList();
    }

    private Map<Task, TaskStatistics> generateTaskStatisticsMap(List<Task> tasks) {
        return tasks.stream().collect(toMap(task -> task, taskStatisticsService::getTaskStatistics));
    }
}
