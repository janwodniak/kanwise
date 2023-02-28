package com.kanwise.kanwise_service.repository.task;

import com.kanwise.kanwise_service.model.task_status.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    Page<TaskStatus> findByTaskId(long taskId, Pageable pageable);
}
