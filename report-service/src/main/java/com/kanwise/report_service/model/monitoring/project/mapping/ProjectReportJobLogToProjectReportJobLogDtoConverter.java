package com.kanwise.report_service.model.monitoring.project.mapping;

import com.kanwise.report_service.controller.job.project.ProjectReportJobController;
import com.kanwise.report_service.controller.subscriber.SubscriberController;
import com.kanwise.report_service.model.monitoring.project.ProjectReportJobLog;
import com.kanwise.report_service.model.monitoring.project.dto.ProjectReportJobLogDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ProjectReportJobLogToProjectReportJobLogDtoConverter implements Converter<ProjectReportJobLog, ProjectReportJobLogDto> {

    private ProjectReportJobLogDto generateProjectReportJobLogDto(ProjectReportJobLog source) {
        return ProjectReportJobLogDto.builder()
                .id(source.getId())
                .jobId(source.getJobInformation().getId())
                .subscriberUsername(source.getJobInformation().getUsername())
                .status(source.getStatus())
                .timestamp(source.getTimestamp())
                .message(source.getMessage())
                .data(source.getData())
                .build();
    }

    @Override
    public ProjectReportJobLogDto convert(MappingContext<ProjectReportJobLog, ProjectReportJobLogDto> context) {
        ProjectReportJobLog projectReportJobLog = context.getSource();
        ProjectReportJobLogDto projectReportJobLogDto = generateProjectReportJobLogDto(projectReportJobLog);
        addHateoasLinks(projectReportJobLog, projectReportJobLogDto);
        return projectReportJobLogDto;
    }

    private void addHateoasLinks(ProjectReportJobLog projectReportJobLog, ProjectReportJobLogDto projectReportJobLogDto) {
        projectReportJobLogDto.add(linkTo(methodOn(SubscriberController.class).getSubscriber(projectReportJobLog.getJobInformation().getUsername())).withRel("subscriber"));
        projectReportJobLogDto.add(linkTo(methodOn(ProjectReportJobController.class).getJob(projectReportJobLog.getJobInformation().getId())).withRel("project-job"));
    }
}

