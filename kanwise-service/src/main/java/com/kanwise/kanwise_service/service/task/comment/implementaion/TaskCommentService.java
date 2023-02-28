package com.kanwise.kanwise_service.service.task.comment.implementaion;

import com.kanwise.kanwise_service.error.custom.task.TaskCommentNotFoundException;
import com.kanwise.kanwise_service.model.task_comment.TaskComment;
import com.kanwise.kanwise_service.repository.task.TaskCommentRepository;
import com.kanwise.kanwise_service.service.task.comment.ITaskCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TaskCommentService implements ITaskCommentService {

    private final TaskCommentRepository taskCommentRepository;

    @Transactional
    @Override
    public TaskComment saveTaskComment(TaskComment taskComment) {
        return taskCommentRepository.saveAndFlush(taskComment);
    }

    @Transactional(readOnly = true)
    @Override
    public TaskComment findById(long id) {
        return taskCommentRepository.findById(id).orElseThrow(TaskCommentNotFoundException::new);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TaskComment> getTaskCommentsByTaskId(long id, Pageable pageable) {
        return taskCommentRepository.findAllByTaskId(id, pageable);
    }
}
