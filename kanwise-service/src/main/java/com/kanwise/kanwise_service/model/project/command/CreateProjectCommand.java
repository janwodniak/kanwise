package com.kanwise.kanwise_service.model.project.command;

import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;


@Builder
public record CreateProjectCommand(
        @NotBlank(message = "TITLE_NOT_BLANK")
        String title,
        @NotBlank(message = "DESCRIPTION_NOT_BLANK")
        String description,
        @NotNull(message = "MEMBERS_USERNAMES_NOT_NULL")
        Set<String> membersUsernames) {
}
