package com.kanwise.report_service.model.subscriber.mapping;

import com.kanwise.report_service.controller.subscriber.SubscriberController;
import com.kanwise.report_service.model.subscriber.Subscriber;
import com.kanwise.report_service.model.subscriber.dto.SubscriberDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static java.util.Optional.empty;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Service
public class SubscriberToSubscriberDtoConverter implements Converter<Subscriber, SubscriberDto> {
    @Override
    public SubscriberDto convert(MappingContext<Subscriber, SubscriberDto> mappingContext) {
        Subscriber subscriber = mappingContext.getSource();
        SubscriberDto subscriberDto = generateSubscriberDto(subscriber);
        addHateoasLinks(subscriber, subscriberDto);
        return subscriberDto;
    }

    private void addHateoasLinks(Subscriber subscriber, SubscriberDto subscriberDto) {
        subscriberDto.add(linkTo(methodOn(SubscriberController.class).getPersonalReports(subscriber.getUsername(), empty())).withRel("personal-reports"));
        subscriberDto.add(linkTo(methodOn(SubscriberController.class).getProjectReports(subscriber.getUsername(), empty())).withRel("project-reports"));
    }

    private SubscriberDto generateSubscriberDto(Subscriber subscriber) {
        return SubscriberDto.builder()
                .username(subscriber.getUsername())
                .email(subscriber.getEmail())
                .personalReportsCount(subscriber.getPersonalReportJobInformation().size())
                .projectReportsCount(subscriber.getProjectReportJobInformation().size())
                .build();
    }
}
