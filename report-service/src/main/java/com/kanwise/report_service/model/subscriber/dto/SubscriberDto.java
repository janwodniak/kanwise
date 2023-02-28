package com.kanwise.report_service.model.subscriber.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class SubscriberDto extends RepresentationModel<SubscriberDto> {
    String username;
    String email;

    int personalReportsCount;
    int projectReportsCount;
}
