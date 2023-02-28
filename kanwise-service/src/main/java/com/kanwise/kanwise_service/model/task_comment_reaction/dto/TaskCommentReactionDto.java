package com.kanwise.kanwise_service.model.task_comment_reaction.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class TaskCommentReactionDto extends RepresentationModel<TaskCommentReactionDto> {
    long taskId;
    long id;
    String authorUsername;
    String reactionLabel;
    LocalDateTime reactedAt;
}
