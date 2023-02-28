package com.kanwise.kanwise_service.model.task_comment_reaction.mapping;

import com.kanwise.kanwise_service.model.task_comment_reaction.ReactionLabel;
import com.kanwise.kanwise_service.model.task_comment_reaction.TaskCommentReaction;
import com.kanwise.kanwise_service.model.task_comment_reaction.command.CreateTaskCommentReactionCommand;
import com.kanwise.kanwise_service.service.member.IMemberService;
import com.kanwise.kanwise_service.service.task.comment.ITaskCommentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.time.Clock;

import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
public class CreateTaskCommentReactionCommandToTaskCommentReactionConverter implements Converter<CreateTaskCommentReactionCommand, TaskCommentReaction> {

    private final ITaskCommentService taskCommentService;
    private final IMemberService memberService;
    private final Clock clock;

    @Override
    public TaskCommentReaction convert(MappingContext<CreateTaskCommentReactionCommand, TaskCommentReaction> mappingContext) {
        CreateTaskCommentReactionCommand command = mappingContext.getSource();
        TaskCommentReaction commentReaction = TaskCommentReaction.builder()
                .author(memberService.findMemberByUsername(command.authorUsername()))
                .reactionLabel(ReactionLabel.valueOf(command.reactionLabel().toUpperCase()))
                .reactedAt(now(clock))
                .build();

        commentReaction.setComment(taskCommentService.findById(command.commentId()));
        return commentReaction;
    }

}
