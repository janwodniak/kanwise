package com.kanwise.kanwise_service.service.task.status.implementaion;

import com.kanwise.kanwise_service.error.custom.task.TaskNotFoundException;
import com.kanwise.kanwise_service.model.task_status.TaskStatus;
import com.kanwise.kanwise_service.repository.task.TaskStatusRepository;
import com.kanwise.kanwise_service.service.task.ITaskService;
import com.kanwise.kanwise_service.service.task.status.ITaskStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
public class TaskStatusService implements ITaskStatusService {

    private final TaskStatusRepository taskStatusRepository;
    private final ITaskService taskService;
    private final Clock clock;

    @Transactional
    @Override
    public TaskStatus createTaskStatus(TaskStatus taskStatus) {
        taskStatus.getTask().updatePenultimateStatusSetTill(now(clock));
        return taskStatusRepository.saveAndFlush(taskStatus);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TaskStatus> getTaskStatusByTaskId(long taskId, Pageable pageable) {
        if (taskService.existsById(taskId)) {
            return taskStatusRepository.findByTaskId(taskId, pageable);
        } else {
            throw new TaskNotFoundException();
        }
    }
}
