package com.kanwise.report_service.model.monitoring.personal.mapping;

import com.kanwise.report_service.controller.job.personal.PersonalReportJobController;
import com.kanwise.report_service.controller.subscriber.SubscriberController;
import com.kanwise.report_service.model.monitoring.personal.PersonalReportJobLog;
import com.kanwise.report_service.model.monitoring.personal.dto.PersonalReportJobLogDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class PersonalReportJobLogToPersonalReportJobLogDtoConverter implements Converter<PersonalReportJobLog, PersonalReportJobLogDto> {

    private PersonalReportJobLogDto generatePersonalReportJobLogDto(PersonalReportJobLog source) {
        return PersonalReportJobLogDto.builder()
                .id(source.getId())
                .subscriberUsername(source.getJobInformation().getUsername())
                .jobId(source.getJobInformation().getId())
                .status(source.getStatus())
                .timestamp(source.getTimestamp())
                .message(source.getMessage())
                .data(source.getData())
                .build();
    }

    @Override
    public PersonalReportJobLogDto convert(MappingContext<PersonalReportJobLog, PersonalReportJobLogDto> mappingContext) {
        PersonalReportJobLog personalReportJobLog = mappingContext.getSource();
        PersonalReportJobLogDto personalReportJobLogDto = generatePersonalReportJobLogDto(personalReportJobLog);
        addHateoasLinks(personalReportJobLog, personalReportJobLogDto);
        return personalReportJobLogDto;
    }

    private void addHateoasLinks(PersonalReportJobLog personalReportJobLog, PersonalReportJobLogDto personalReportJobLogDto) {
        personalReportJobLogDto.add(linkTo(methodOn(SubscriberController.class).getSubscriber(personalReportJobLog.getJobInformation().getUsername())).withRel("subscriber"));
        personalReportJobLogDto.add(linkTo(methodOn(PersonalReportJobController.class).getJob(personalReportJobLog.getJobInformation().getId())).withRel("personal-job"));
    }
}
