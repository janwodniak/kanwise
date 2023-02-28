package com.kanwise.report_service.model.subscriber.command;

import com.kanwise.report_service.validation.annotation.email.EmailPattern;
import lombok.Builder;

import javax.validation.constraints.NotBlank;

@Builder
public record CreateSubscriberCommand(
        @NotBlank(message = "USERNAME_NOT_BLANK") String username,
        @NotBlank(message = "EMAIL_NOT_BLANK") @EmailPattern String email
) {
}
