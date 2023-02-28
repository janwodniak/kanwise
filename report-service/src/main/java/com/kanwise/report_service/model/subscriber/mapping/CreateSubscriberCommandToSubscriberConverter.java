package com.kanwise.report_service.model.subscriber.mapping;

import com.kanwise.report_service.model.subscriber.Subscriber;
import com.kanwise.report_service.model.subscriber.command.CreateSubscriberCommand;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static java.lang.Boolean.TRUE;


@Service
public class CreateSubscriberCommandToSubscriberConverter implements Converter<CreateSubscriberCommand, Subscriber> {

    @Override
    public Subscriber convert(MappingContext<CreateSubscriberCommand, Subscriber> mappingContext) {
        CreateSubscriberCommand source = mappingContext.getSource();
        return Subscriber.builder()
                .username(source.username())
                .email(source.email())
                .active(TRUE)
                .build();
    }
}
