package com.kanwise.kanwise_service.service.task.implementation;

import com.kanwise.kanwise_service.error.custom.task.TaskNotFoundException;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.membership.Membership;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task.TaskPriority;
import com.kanwise.kanwise_service.model.task.TaskType;
import com.kanwise.kanwise_service.model.task.command.EditTaskCommand;
import com.kanwise.kanwise_service.model.task.command.EditTaskPartiallyCommand;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import com.kanwise.kanwise_service.repository.task.TaskRepository;
import com.kanwise.kanwise_service.service.membership.IMembershipService;
import com.kanwise.kanwise_service.service.task.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class TaskService implements ITaskService {

    private final TaskRepository taskRepository;
    private final IMembershipService membershipService;

    @Override
    public Task findTaskById(long id) {
        return taskRepository.findById(id).orElseThrow(TaskNotFoundException::new);
    }

    @Override
    public Page<Task> findTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    @Transactional
    @Override
    public Task createTask(Task task) {
        return taskRepository.saveAndFlush(task);
    }

    @Transactional
    @Override
    public void deleteTask(long id) {
        if (taskRepository.existsActiveById(id)) {
            taskRepository.softDeleteById(id);
        } else {
            throw new TaskNotFoundException();
        }
    }

    @Transactional
    @Override
    public Task editTask(long id, EditTaskCommand command) {
        return taskRepository.findById(id).map(taskToEdit -> {
            taskToEdit.setTitle(command.title());
            taskToEdit.setType(TaskType.valueOf(command.type().toUpperCase()));
            taskToEdit.setDescription(command.description());
            taskToEdit.setEstimatedTime(command.estimatedTime());
            taskToEdit.setCurrentStatus(TaskStatusLabel.valueOf(command.currentStatus().toUpperCase()));
            taskToEdit.setPriority(TaskPriority.valueOf(command.priority().toUpperCase()));
            return taskToEdit;
        }).orElseThrow(TaskNotFoundException::new);
    }

    @Transactional
    @Override
    public Task editTaskPartially(long id, EditTaskPartiallyCommand command) {
        return taskRepository.findById(id).map(taskToEdit -> {
            ofNullable(command.title()).ifPresent(taskToEdit::setTitle);
            ofNullable(command.type()).ifPresent(type -> taskToEdit.setType(TaskType.valueOf(type.toUpperCase())));
            ofNullable(command.description()).ifPresent(taskToEdit::setDescription);
            ofNullable(command.estimatedTime()).ifPresent(taskToEdit::setEstimatedTime);
            ofNullable(command.currentStatus()).ifPresent(status -> taskToEdit.setCurrentStatus(TaskStatusLabel.valueOf(status.toUpperCase())));
            ofNullable(command.priority()).ifPresent(priority -> taskToEdit.setPriority(TaskPriority.valueOf(priority.toUpperCase())));
            return taskToEdit;
        }).orElseThrow(TaskNotFoundException::new);
    }

    @Override
    public Set<Member> findAssignedMembers(long id) {
        return findTaskById(id)
                .getAssignedMemberships()
                .stream()
                .map(Membership::getMember)
                .collect(HashSet::new, Set::add, Set::addAll);
    }

    @Transactional
    @Override
    public Set<Member> assignMembersToTask(long id, Set<String> usernames) {
        Task task = findTaskById(id);
        Set<Membership> memberships = membershipService.findMembershipsByProjectAndUsernames(task.getProject().getId(), usernames);


        return memberships.stream()
                .map(membership -> {
                    membership.assignTask(task);
                    return membership.getMember();
                })
                .collect(toSet());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsById(long id) {
        return taskRepository.existsById(id);
    }
}
