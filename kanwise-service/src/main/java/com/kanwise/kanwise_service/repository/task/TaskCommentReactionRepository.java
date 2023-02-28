package com.kanwise.kanwise_service.repository.task;

import com.kanwise.kanwise_service.model.task_comment_reaction.TaskCommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskCommentReactionRepository extends JpaRepository<TaskCommentReaction, Long> {
    Optional<TaskCommentReaction> findByAuthorUsernameAndCommentId(String username, Long id);
}
