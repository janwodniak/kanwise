package com.kanwise.kanwise_service.model.member.command;

import com.kanwise.kanwise_service.model.notification.ProjectNotificationType;
import com.kanwise.kanwise_service.validation.annotation.member.UniqueUsername;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Builder
public record EditMemberCommand(

        @UniqueUsername
        @NotBlank(message = "USERNAME_NOT_BLANK") String username,
        @NotNull(message = "NOTIFICATION_SUBSCRIPTIONS_NOT_NULL") Map<ProjectNotificationType, Boolean> notificationSubscriptions
) {
}