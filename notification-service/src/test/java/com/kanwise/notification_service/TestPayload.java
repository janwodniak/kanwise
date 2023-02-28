package com.kanwise.notification_service;


import lombok.Builder;

@Builder
public record TestPayload<T>(T payload, String expectedMessage) {
}
