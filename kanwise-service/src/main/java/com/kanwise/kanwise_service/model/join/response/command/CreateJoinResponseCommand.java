package com.kanwise.kanwise_service.model.join.response.command;

import com.kanwise.kanwise_service.model.join.request.JoinRequestStatus;
import com.kanwise.kanwise_service.validation.annotation.common.ValueOfEnum;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
public record CreateJoinResponseCommand(

        @NotBlank(message = "RESPONDED_BY_USERNAME_NOT_BLANK")
        String respondedByUsername,
        @NotNull(message = "JOIN_REQUEST_ID_NOT_NULL")
        Long joinRequestId,
        @NotBlank(message = "STATUS_NOT_NULL")
        @ValueOfEnum(enumClass = JoinRequestStatus.class)
        String status,
        @NotBlank(message = "MESSAGE_NOT_BLANK")
        String message) {

}

