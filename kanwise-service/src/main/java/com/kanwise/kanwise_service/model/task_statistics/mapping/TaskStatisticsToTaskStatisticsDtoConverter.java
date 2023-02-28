package com.kanwise.kanwise_service.model.task_statistics.mapping;

import com.kanwise.kanwise_service.controller.project.ProjectController;
import com.kanwise.kanwise_service.controller.task.TaskController;
import com.kanwise.kanwise_service.model.task_statistics.TaskStatistics;
import com.kanwise.kanwise_service.model.task_statistics.dto.TaskStatisticsDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class TaskStatisticsToTaskStatisticsDtoConverter implements Converter<TaskStatistics, TaskStatisticsDto> {

    @Override
    public TaskStatisticsDto convert(MappingContext<TaskStatistics, TaskStatisticsDto> mappingContext) {
        TaskStatistics taskStatistics = mappingContext.getSource();
        TaskStatisticsDto taskStatisticsDto = TaskStatisticsDto.builder()
                .projectId(taskStatistics.getTask().getProject().getId())
                .taskId(taskStatistics.getTask().getId())
                .assignedMembersCount(taskStatistics.getTask().getAssignedMemberships().size())
                .commentsCount(taskStatistics.getTask().getComments().size())
                .statusesCount(taskStatistics.getTask().getStatuses().size())
                .estimatedTime(taskStatistics.getEstimatedTime())
                .totalExistenceTime(taskStatistics.getTotalExistenceTime())
                .taskStatusDurationMap(taskStatistics.getTaskStatusDurationMap())
                .build();

        addHateoasLinks(taskStatistics, taskStatisticsDto);
        return taskStatisticsDto;
    }

    private void addHateoasLinks(TaskStatistics taskStatistics, TaskStatisticsDto taskStatisticsDto) {
        taskStatisticsDto.add(linkTo(methodOn(ProjectController.class).findProjectById(taskStatistics.getTask().getProject().getId())).withRel("project"));
        taskStatisticsDto.add(linkTo(methodOn(TaskController.class).findTask(taskStatistics.getTask().getId())).withRel("task"));
    }
}
