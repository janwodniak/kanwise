package com.kanwise.kanwise_service.service.task.status;

import com.kanwise.kanwise_service.model.task_status.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITaskStatusService {

    TaskStatus createTaskStatus(TaskStatus taskStatus);


    Page<TaskStatus> getTaskStatusByTaskId(long taskId, Pageable pageable);
}
