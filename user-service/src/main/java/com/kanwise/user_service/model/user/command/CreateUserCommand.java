package com.kanwise.user_service.model.user.command;

import com.kanwise.user_service.validation.annotation.email.EmailPattern;
import com.kanwise.user_service.validation.annotation.email.UniqueEmail;
import com.kanwise.user_service.validation.annotation.username.UniqueUsername;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
public record CreateUserCommand(
        @NotBlank(message = "FIRST_NAME_NOT_BLANK") String firstName,
        @NotBlank(message = "LAST_NAME_NOT_BLANK") String lastName,
        @NotBlank(message = "USERNAME_NOT_BLANK") @UniqueUsername String username,
        @NotNull(message = "EMAIL_NOT_NULL") @UniqueEmail @EmailPattern String email
) {
}
