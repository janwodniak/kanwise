package com.kanwise.clients.report_service.subscriber.client;

import lombok.Builder;

@Builder
public record SubscriberDto(String username, String email) {

}
