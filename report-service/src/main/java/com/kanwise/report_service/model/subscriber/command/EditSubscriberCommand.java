package com.kanwise.report_service.model.subscriber.command;

import com.kanwise.report_service.validation.annotation.email.EmailPattern;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
public record EditSubscriberCommand(
        @NotBlank(message = "USERNAME_NOT_BLANK") String username,
        @NotNull(message = "EMAIL_NOT_NULL")
        @EmailPattern
        String email
) {
}
