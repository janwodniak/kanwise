package com.kanwise.kanwise_service.model.member.command;

import com.kanwise.kanwise_service.validation.annotation.member.UniqueUsername;

import javax.validation.constraints.NotBlank;

public record CreateMemberCommand(
        @UniqueUsername
        @NotBlank(message = "USERNAME_NOT_BLANK")
        String username) {
}