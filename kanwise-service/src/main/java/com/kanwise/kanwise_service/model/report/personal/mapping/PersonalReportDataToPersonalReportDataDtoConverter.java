package com.kanwise.kanwise_service.model.report.personal.mapping;

import com.kanwise.clients.report_service.report.model.personal.PersonalReportDataDto;
import com.kanwise.clients.report_service.report.model.task.TaskDataDto;
import com.kanwise.kanwise_service.model.report.personal.PersonalReportData;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.kanwise.kanwise_service.model.task_status.TaskStatusLabel.IN_PROGRESS;


@Service
public class PersonalReportDataToPersonalReportDataDtoConverter implements Converter<PersonalReportData, PersonalReportDataDto> {

    @Override
    public PersonalReportDataDto convert(MappingContext<PersonalReportData, PersonalReportDataDto> mappingContext) {
        PersonalReportData source = mappingContext.getSource();
        return PersonalReportDataDto.builder()
                .data(constructData(source))
                .build();
    }

    private Map<String, Object> constructData(PersonalReportData source) {
        return Map.of(
                "startDate", source.getStartDate(),
                "endDate", source.getEndDate(),
                "tasks", getTasksData(source.getTasks()),
                "totalPerformance", getTotalPerformance(source.getTasks()),
                "totalEstimatedTime", getTotalEstimatedTime(source.getTasks()),
                "totalActualTime", getTotalActualTime(source.getTasks())
        );
    }

    private Long getTotalActualTime(Map<Task, TaskStatistics> tasks) {
        return tasks.values().stream()
                .map(taskStatistics -> taskStatistics.getTimeInStatus(IN_PROGRESS))
                .mapToLong(Duration::toSeconds)
                .sum();
    }

    private Long getTotalEstimatedTime(Map<Task, TaskStatistics> tasks) {
        return tasks.values().stream()
                .map(TaskStatistics::getEstimatedTime)
                .mapToLong(Duration::toSeconds)
                .sum();
    }

    private Long getTotalPerformance(Map<Task, TaskStatistics> tasks) {
        return tasks.values().stream()
                .map(taskStatistics -> taskStatistics.getTimeInStatus(IN_PROGRESS).toSeconds() * 100 / taskStatistics.getEstimatedTime().toSeconds())
                .mapToLong(Long::longValue)
                .sum();
    }


    private List<TaskDataDto> getTasksData(Map<Task, TaskStatistics> tasks) {
        return tasks.entrySet()
                .stream()
                .map(this::getTaskData)
                .toList();
    }

    private TaskDataDto getTaskData(Map.Entry<Task, TaskStatistics> entry) {
        Task task = entry.getKey();
        TaskStatistics taskStatistics = entry.getValue();
        return TaskDataDto.builder()
                .title(task.getTitle())
                .projectTitle(task.getProject().getTitle())
                .createdAt(task.getCreatedAt())
                .estimatedTime(taskStatistics.getEstimatedTime())
                .actualTime(taskStatistics.getTimeInStatus(IN_PROGRESS))
                .performance(100)
                .build();
    }
}
