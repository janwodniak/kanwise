package com.kanwise.kanwise_service.model.project.mapping;

import com.kanwise.kanwise_service.controller.project.ProjectController;
import com.kanwise.kanwise_service.controller.project.ProjectStatisticsController;
import com.kanwise.kanwise_service.model.project.Project;
import com.kanwise.kanwise_service.model.project.dto.ProjectDto;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static com.kanwise.kanwise_service.model.task_status.TaskStatusLabel.IN_PROGRESS;
import static com.kanwise.kanwise_service.model.task_status.TaskStatusLabel.RESOLVED;
import static com.kanwise.kanwise_service.model.task_status.TaskStatusLabel.TODO;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ProjectToProjectDtoConverter implements Converter<Project, ProjectDto> {

    private static void addHateoasLinks(Project project, ProjectDto projectDto) {
        projectDto.add(linkTo(methodOn(ProjectController.class).getProjectMembers(project.getId())).withRel("project-members"));
        projectDto.add(linkTo(methodOn(ProjectController.class).getProjectTasks(project.getId())).withRel("project-tasks"));
        projectDto.add(linkTo(methodOn(ProjectStatisticsController.class).getProjectStatistics(project.getId())).withRel("project-statistics"));

    }

    @Override
    public ProjectDto convert(MappingContext<Project, ProjectDto> mappingContext) {
        Project project = mappingContext.getSource();
        ProjectDto projectDto = ProjectDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .membersCount(project.getMembers().size())
                .tasksCount(project.getTasks().size())
                .todoTaskCount(getCountOfTasksByStatus(project, TODO))
                .inProgressTaskCount(getCountOfTasksByStatus(project, IN_PROGRESS))
                .doneTaskCount(getCountOfTasksByStatus(project, RESOLVED))
                .joinRequestsCount(project.getJoinRequests().stream().filter(joinRequest -> !joinRequest.isResponded()).count())
                .status(project.getStatus())
                .build();

        addHateoasLinks(project, projectDto);
        return projectDto;
    }

    private long getCountOfTasksByStatus(Project project, TaskStatusLabel status) {
        return project.getTasks().stream()
                .map(Task::getCurrentStatus)
                .filter(taskStatusLabel -> taskStatusLabel.equals(status))
                .count();
    }
}
