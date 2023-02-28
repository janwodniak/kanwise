package com.kanwise.clients.report_service.subscriber.model;

import lombok.Builder;

@Builder
public record EditSubscriberPartiallyCommand(
        String username
) {
}
