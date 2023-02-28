package com.kanwise.user_service.test;


import lombok.Builder;

@Builder
public
record TestPayload<T>(T payload, String expectedMessage) {
}
