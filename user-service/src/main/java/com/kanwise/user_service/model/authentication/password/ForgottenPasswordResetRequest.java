package com.kanwise.user_service.model.authentication.password;

import com.kanwise.user_service.validation.annotation.email.EmailExists;

import javax.validation.constraints.NotBlank;


public record ForgottenPasswordResetRequest(
        @NotBlank(message = "EMAIL_NOT_BLANK") @EmailExists String email) {
}
