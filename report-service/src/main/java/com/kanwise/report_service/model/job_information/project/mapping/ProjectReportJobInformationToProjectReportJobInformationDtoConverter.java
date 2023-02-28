package com.kanwise.report_service.model.job_information.project.mapping;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.kanwise.report_service.controller.subscriber.SubscriberController;
import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.model.job_information.project.dto.ProjectReportJobInformationDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.util.Locale;

import static com.cronutils.model.CronType.QUARTZ;
import static java.util.Optional.empty;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Service
public class ProjectReportJobInformationToProjectReportJobInformationDtoConverter implements Converter<ProjectReportJobInformation, ProjectReportJobInformationDto> {
    private static ProjectReportJobInformationDto.ProjectReportJobInformationDtoBuilder generateBaseProjectReportJobInformationDtoBuilder(ProjectReportJobInformation jobInfo) {
        return ProjectReportJobInformationDto.builder()
                .id(jobInfo.getId())
                .projectId(jobInfo.getProjectId())
                .subscriberUsername(jobInfo.getUsername())
                .startDate(jobInfo.getStartDate())
                .endDate(jobInfo.getEndDate())
                .status(jobInfo.getStatus());
    }

    @Override
    public ProjectReportJobInformationDto convert(MappingContext<ProjectReportJobInformation, ProjectReportJobInformationDto> mappingContext) {
        ProjectReportJobInformation projectReportJobInformation = mappingContext.getSource();
        ProjectReportJobInformationDto projectReportJobInformationDto = generateProjectReportJobInformationDto(projectReportJobInformation);
        addHateoasLinks(projectReportJobInformation, projectReportJobInformationDto);
        return projectReportJobInformationDto;
    }

    private void addHateoasLinks(ProjectReportJobInformation projectReportJobInformation, ProjectReportJobInformationDto projectReportJobInformationDto) {
        projectReportJobInformationDto.add(linkTo(methodOn(SubscriberController.class).getSubscriber(projectReportJobInformation.getSubscriber().getUsername())).withRel("subscriber"));
        projectReportJobInformationDto.add(linkTo(methodOn(SubscriberController.class).getPersonalReports(projectReportJobInformation.getSubscriber().getUsername(), empty())).withRel("personal-reports"));
        projectReportJobInformationDto.add(linkTo(methodOn(SubscriberController.class).getProjectReports(projectReportJobInformation.getSubscriber().getUsername(), empty())).withRel("project-reports"));
    }

    private ProjectReportJobInformationDto generateProjectReportJobInformationDto(ProjectReportJobInformation source) {
        if (source.isCronBased()) {
            return generateCronJobInformationDto(source);
        } else if (source.isRunForever()) {
            return generateRunForeverJobInformationDto(source);
        } else {
            return generateFireCountJobInformationDto(source);
        }
    }

    private ProjectReportJobInformationDto generateFireCountJobInformationDto(ProjectReportJobInformation jobInfo) {
        return generateBaseProjectReportJobInformationDtoBuilder(jobInfo)
                .totalFireCount(jobInfo.getTotalFireCount())
                .remainingFireCount(jobInfo.getRemainingFireCount())
                .initialOffsetMs(jobInfo.getInitialOffsetMs())
                .repeatInterval(jobInfo.getRepeatInterval())
                .runForever(jobInfo.isRunForever())
                .build();
    }

    private ProjectReportJobInformationDto generateRunForeverJobInformationDto(ProjectReportJobInformation jobInfo) {
        return generateBaseProjectReportJobInformationDtoBuilder(jobInfo)
                .runForever(jobInfo.isRunForever())
                .initialOffsetMs(jobInfo.getInitialOffsetMs())
                .repeatInterval(jobInfo.getRepeatInterval())
                .build();
    }

    private ProjectReportJobInformationDto generateCronJobInformationDto(ProjectReportJobInformation jobInfo) {
        return generateBaseProjectReportJobInformationDtoBuilder(jobInfo)
                .cron(jobInfo.getCron())
                .description(generateCronDescription(jobInfo.getCron()))
                .build();
    }

    private String generateCronDescription(String cron) {
        CronDescriptor descriptor = CronDescriptor.instance(Locale.ENGLISH);
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        return descriptor.describe(parser.parse(cron));
    }
}
