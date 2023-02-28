package com.kanwise.kanwise_service.model.project_statistics.mapping;

import com.kanwise.kanwise_service.controller.project.ProjectController;
import com.kanwise.kanwise_service.model.project_statistics.ProjectStatistics;
import com.kanwise.kanwise_service.model.project_statistics.dto.ProjectStatisticsDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ProjectStatisticsToProjectStatisticsDtoConverter implements Converter<ProjectStatistics, ProjectStatisticsDto> {
    @Override
    public ProjectStatisticsDto convert(MappingContext<ProjectStatistics, ProjectStatisticsDto> mappingContext) {
        ProjectStatistics projectStatistics = mappingContext.getSource();
        ProjectStatisticsDto projectStatisticsDto = ProjectStatisticsDto.builder()
                .projectId(projectStatistics.getProject().getId())
                .totalTasksCount(projectStatistics.getTotalTasksStatusCountMap().values().stream().mapToLong(Long::longValue).sum())
                .totalEstimatedTime(projectStatistics.getTotalEstimatedTime())
                .totalTasksStatusCountMap(projectStatistics.getTotalTasksStatusCountMap())
                .totalTasksStatusDurationMap(projectStatistics.getTotalTasksStatusDurationMap())
                .totalTasksTypeCountMap(projectStatistics.getTotalTasksTypeCountMap())
                .performancePercentage(projectStatistics.getPerformancePercentage())
                .build();

        addHateoasLinks(projectStatistics, projectStatisticsDto);
        return projectStatisticsDto;
    }

    private void addHateoasLinks(ProjectStatistics projectStatistics, ProjectStatisticsDto projectStatisticsDto) {
        projectStatisticsDto.add(linkTo(methodOn(ProjectController.class).getProjectMembers(projectStatistics.getProject().getId())).withRel("project-members"));
        projectStatisticsDto.add(linkTo(methodOn(ProjectController.class).getProjectTasks(projectStatistics.getProject().getId())).withRel("project-tasks"));
    }
}
