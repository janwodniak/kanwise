package com.kanwise.user_service.model.authentication.request;

import lombok.Builder;

import javax.validation.constraints.NotBlank;

@Builder
public record LoginRequest(
        @NotBlank(message = "USERNAME_NOT_BLANK") String username,
        @NotBlank(message = "PASSWORD_NOT_BLANK") String password
) {
}
