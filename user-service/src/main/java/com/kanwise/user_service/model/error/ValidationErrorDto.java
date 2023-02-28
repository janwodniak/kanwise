package com.kanwise.user_service.model.error;

import lombok.Builder;

@Builder
public record ValidationErrorDto(String field, String message) {
}