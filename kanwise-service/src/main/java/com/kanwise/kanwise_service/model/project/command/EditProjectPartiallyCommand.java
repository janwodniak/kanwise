package com.kanwise.kanwise_service.model.project.command;


import com.kanwise.kanwise_service.model.project.ProjectStatus;
import com.kanwise.kanwise_service.validation.annotation.common.NullOrNotBlank;
import com.kanwise.kanwise_service.validation.annotation.common.ValueOfEnum;
import lombok.Builder;

@Builder
public record EditProjectPartiallyCommand(
        @NullOrNotBlank(message = "TITLE_NULL_OR_NOT_BLANK")
        String title,
        @NullOrNotBlank(message = "DESCRIPTION_NULL_OR_NOT_BLANK")
        String description,
        @ValueOfEnum(enumClass = ProjectStatus.class)
        String status) {
}