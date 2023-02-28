package com.kanwise.report_service.model.job_information.personal.mapping;

import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.job_information.personal.request.PersonalReportJobRequest;
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
public class PersonalReportJobRequestToPersonalReportJobInformationConverter implements Converter<PersonalReportJobRequest, PersonalReportJobInformation> {

    private final SubscriberService subscriberService;
    private final Clock clock;

    @Override
    public PersonalReportJobInformation convert(MappingContext<PersonalReportJobRequest, PersonalReportJobInformation> mappingContext) {
        PersonalReportJobRequest request = mappingContext.getSource();
        return PersonalReportJobInformation.builder()
                .username(request.username())
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
