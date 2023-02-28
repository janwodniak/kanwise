package com.kanwise.kanwise_service.model.task_comment_reaction.command;

import com.kanwise.kanwise_service.model.task_comment_reaction.ReactionLabel;
import com.kanwise.kanwise_service.validation.annotation.common.ValueOfEnum;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Builder
public record CreateTaskCommentReactionCommand(
        @NotBlank(message = "AUTHOR_USERNAME_NOT_BLANK")
        String authorUsername,
        @NotNull(message = "TASK_COMMENT_ID_NOT_NULL")
        Long commentId,
        @NotNull(message = "REACTION_LABEL_NOT_NULL")
        @ValueOfEnum(enumClass = ReactionLabel.class)
        String reactionLabel) {
}

