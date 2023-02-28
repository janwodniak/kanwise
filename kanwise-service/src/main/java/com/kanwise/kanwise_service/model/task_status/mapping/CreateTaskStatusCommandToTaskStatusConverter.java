package com.kanwise.kanwise_service.model.task_status.mapping;

import com.kanwise.kanwise_service.model.task_status.TaskStatus;
import com.kanwise.kanwise_service.model.task_status.TaskStatusLabel;
import com.kanwise.kanwise_service.model.task_status.command.CreateTaskStatusCommand;
import com.kanwise.kanwise_service.service.member.IMemberService;
import com.kanwise.kanwise_service.service.task.ITaskService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.time.Clock;

import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
public class CreateTaskStatusCommandToTaskStatusConverter implements Converter<CreateTaskStatusCommand, TaskStatus> {

    private final ITaskService taskService;
    private final IMemberService memberService;
    private final Clock clock;

    @Override
    public TaskStatus convert(MappingContext<CreateTaskStatusCommand, TaskStatus> mappingContext) {
        CreateTaskStatusCommand command = mappingContext.getSource();
        TaskStatus taskStatus = TaskStatus.builder()
                .label(TaskStatusLabel.valueOf(command.label().toUpperCase()))
                .setAt(now(clock))
                .setBy(memberService.findMemberByUsername(command.setBy()))
                .build();

        taskStatus.setTask(taskService.findTaskById(command.taskId()));
        return taskStatus;
    }
}
