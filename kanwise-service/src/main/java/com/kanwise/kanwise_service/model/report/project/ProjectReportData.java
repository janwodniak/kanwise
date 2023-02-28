package com.kanwise.kanwise_service.model.report.project;

import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.report.ReportData;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@Setter
@SuperBuilder
public class ProjectReportData extends ReportData {
    private Project project;
    private Map<Task, TaskStatistics> tasks;
}
