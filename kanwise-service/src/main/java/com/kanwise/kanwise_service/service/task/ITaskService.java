package com.kanwise.kanwise_service.service.task;

import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task.command.EditTaskCommand;
import com.kanwise.kanwise_service.model.task.command.EditTaskPartiallyCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ITaskService {
    Task findTaskById(long id);

    Page<Task> findTasks(Pageable pageable);

    Task createTask(Task map);

    void deleteTask(long id);

    Task editTask(long id, EditTaskCommand command);

    Task editTaskPartially(long id, EditTaskPartiallyCommand command);

    Set<Member> findAssignedMembers(long id);

    Set<Member> assignMembersToTask(long id, Set<String> usernames);

    boolean existsById(long id);
}
