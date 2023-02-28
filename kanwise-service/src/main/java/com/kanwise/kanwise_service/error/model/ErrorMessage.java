package com.kanwise.kanwise_service.error.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ErrorMessage {
    private LocalDateTime timestamp;
    private int code;
    private String status;
    private String message;
    private String uri;
    private String method;
}
