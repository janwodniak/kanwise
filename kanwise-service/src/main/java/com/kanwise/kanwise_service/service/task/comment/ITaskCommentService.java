package com.kanwise.kanwise_service.service.task.comment;

import com.kanwise.kanwise_service.model.task_comment.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITaskCommentService {
    TaskComment saveTaskComment(TaskComment taskComment);

    TaskComment findById(long id);

    Page<TaskComment> getTaskCommentsByTaskId(long id, Pageable pageable);
}
