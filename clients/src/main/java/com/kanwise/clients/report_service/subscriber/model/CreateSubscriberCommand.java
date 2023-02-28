package com.kanwise.clients.report_service.subscriber.model;

import lombok.Builder;

@Builder
public record CreateSubscriberCommand(String username, String email) {
}
