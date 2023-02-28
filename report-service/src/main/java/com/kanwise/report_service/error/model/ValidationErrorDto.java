package com.kanwise.report_service.error.model;

public record ValidationErrorDto(
        String field,
        String message) {
}
