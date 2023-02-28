package com.kanwise.kanwise_service.repository.task;

import com.kanwise.kanwise_service.model.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsActiveById(long id);

    @Modifying
    @Query("UPDATE Task t SET t.active = FALSE WHERE t.id = ?1")
    void softDeleteById(long id);
}
