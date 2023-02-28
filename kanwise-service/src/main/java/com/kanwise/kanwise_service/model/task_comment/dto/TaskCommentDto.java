package com.kanwise.kanwise_service.model.task_comment.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class TaskCommentDto extends RepresentationModel<TaskCommentDto> {
    long id;
    String authorUsername;
    long taskId;
    String content;
    LocalDateTime commentedAt;
    long likesCount;
    long dislikesCount;
}
