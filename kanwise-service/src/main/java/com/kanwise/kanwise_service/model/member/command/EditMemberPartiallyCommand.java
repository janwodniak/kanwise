package com.kanwise.kanwise_service.model.member.command;

import com.kanwise.kanwise_service.model.notification.ProjectNotificationType;
import com.kanwise.kanwise_service.validation.annotation.common.NullOrNotBlank;
import com.kanwise.kanwise_service.validation.annotation.member.UniqueUsername;
import lombok.Builder;

import java.util.Map;

@Builder
public record EditMemberPartiallyCommand(
        @NullOrNotBlank(message = "USERNAME_NULL_OR_NOT_BLANK")

        @UniqueUsername String username,
        Map<ProjectNotificationType, Boolean> notificationSubscriptions
) {
}
