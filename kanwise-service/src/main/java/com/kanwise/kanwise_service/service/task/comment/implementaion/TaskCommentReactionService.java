package com.kanwise.kanwise_service.service.task.comment.implementaion;

import com.kanwise.kanwise_service.error.custom.task.AlreadyReactedToComment;
import com.kanwise.kanwise_service.model.task_comment_reaction.TaskCommentReaction;
import com.kanwise.kanwise_service.repository.task.TaskCommentReactionRepository;
import com.kanwise.kanwise_service.service.task.comment.ITaskCommentReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class TaskCommentReactionService implements ITaskCommentReactionService {

    private final TaskCommentReactionRepository taskCommentReactionRepository;

    @Transactional(dontRollbackOn = AlreadyReactedToComment.class)
    @Override
    public TaskCommentReaction saveTaskCommentReaction(TaskCommentReaction reaction) {
        return taskCommentReactionRepository.findByAuthorUsernameAndCommentId(reaction.getAuthor().getUsername(), reaction.getComment().getId())
                .map(existingReaction -> handleExistingCommentReaction(reaction, existingReaction))
                .orElse(taskCommentReactionRepository.save(reaction));
    }

    private TaskCommentReaction handleExistingCommentReaction(TaskCommentReaction reaction, TaskCommentReaction existingReaction) {
        taskCommentReactionRepository.delete(existingReaction);
        if (existingReaction.getReactionLabel() == reaction.getReactionLabel()) {
            throw new AlreadyReactedToComment();
        }
        return taskCommentReactionRepository.save(reaction);
    }
}
