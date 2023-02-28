package com.kanwise.clients.user_service.user.model;

import lombok.Builder;

import java.util.Map;

@Builder
public record UserDataDto(Map<String, Object> data) {
}
