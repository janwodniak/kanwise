package com.kanwise.report_service.model.subscriber.command;

import com.kanwise.report_service.validation.annotation.common.NullOrNotBlank;
import com.kanwise.report_service.validation.annotation.email.EmailPattern;
import lombok.Builder;

@Builder
public record EditSubscriberPartiallyCommand(
        @NullOrNotBlank(message = "USERNAME_NULL_OR_NOT_BLANK") String username,
        @EmailPattern
        @NullOrNotBlank(message = "EMAIL_NULL_OR_NOT_BLANK") String email
) {
}