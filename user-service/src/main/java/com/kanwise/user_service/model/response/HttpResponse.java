package com.kanwise.user_service.model.response;

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
