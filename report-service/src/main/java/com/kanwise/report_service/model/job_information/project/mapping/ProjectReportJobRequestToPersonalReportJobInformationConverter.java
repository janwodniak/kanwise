package com.kanwise.report_service.model.job_information.project.mapping;

import com.kanwise.report_service.model.job_information.project.ProjectReportJobInformation;
import com.kanwise.report_service.model.job_information.project.request.ProjectReportJobRequest;
import com.kanwise.report_service.service.subscriber.implementation.SubscriberService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.time.Clock;

import static com.kanwise.report_service.model.job_information.common.JobStatus.CREATED;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
public class ProjectReportJobRequestToPersonalReportJobInformationConverter implements Converter<ProjectReportJobRequest, ProjectReportJobInformation> {

    private final SubscriberService subscriberService;
    private final Clock clock;

    @Override
    public ProjectReportJobInformation convert(MappingContext<ProjectReportJobRequest, ProjectReportJobInformation> mappingContext) {
        ProjectReportJobRequest request = mappingContext.getSource();
        return ProjectReportJobInformation.builder()
                .username(request.username())
                .projectId(request.projectId())
                .createdAt(now(clock))
                .status(CREATED)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .subscriber(subscriberService.getSubscriber(request.username()))
                .totalFireCount(request.totalFireCount())
                .remainingFireCount(request.totalFireCount())
                .runForever(request.runForever())
                .repeatInterval(request.repeatInterval())
                .initialOffsetMs(request.initialOffsetMs())
                .cron(request.cron())
                .active(TRUE)
                .build();
    }
}
