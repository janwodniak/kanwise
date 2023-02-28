package com.kanwise.api_gateway.model.http.response;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record HttpResponse(
        LocalDateTime timestamp,
        int httpStatusCode,
        HttpStatus httpStatus,
        String reason,
        String message) {
}

