package com.kanwise.user_service.model.jwt;

import javax.validation.constraints.NotBlank;

public record TokenValidationRequest(
        @NotBlank(message = "TOKEN_NOT_BLANK") String token) {
}