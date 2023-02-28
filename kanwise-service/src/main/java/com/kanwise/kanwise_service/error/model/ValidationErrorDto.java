package com.kanwise.kanwise_service.error.model;

public record ValidationErrorDto(
        String field,
        String message) {
}
