package com.kanwise.kanwise_service.service.task.comment;

import com.kanwise.kanwise_service.model.task_comment_reaction.TaskCommentReaction;

public interface ITaskCommentReactionService {
    TaskCommentReaction saveTaskCommentReaction(TaskCommentReaction reaction);
}
