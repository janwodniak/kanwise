package com.kanwise.kanwise_service.repository.task;

import com.kanwise.kanwise_service.model.task_comment.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    Page<TaskComment> findAllByTaskId(long id, Pageable pageable);
}
