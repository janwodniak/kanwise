package com.kanwise.kanwise_service.model.join.request.command;

import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
public record CreateJoinRequestCommand(
        @NotNull(message = "PROJECT_ID_NOT_NULL")
        Long projectId,
        @NotBlank(message = "REQUESTED_BY_USERNAME_NOT_BLANK")
        String requestedByUsername,
        @NotBlank(message = "MESSAGE_NOT_BLANK")
        String message) {
}
