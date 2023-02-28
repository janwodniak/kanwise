package com.kanwise.kanwise_service.model.project.command;

import com.kanwise.kanwise_service.model.project.ProjectStatus;
import com.kanwise.kanwise_service.validation.annotation.common.ValueOfEnum;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
public record EditProjectCommand(

        @NotBlank(message = "TITLE_NOT_BLANK")
        String title,
        @NotBlank(message = "DESCRIPTION_NOT_BLANK")
        String description,
        @NotNull(message = "STATUS_NOT_NULL")
        @ValueOfEnum(enumClass = ProjectStatus.class)
        String status) {
}
