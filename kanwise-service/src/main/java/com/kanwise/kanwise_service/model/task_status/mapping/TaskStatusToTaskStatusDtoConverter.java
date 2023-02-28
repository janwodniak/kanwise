package com.kanwise.kanwise_service.model.task_status.mapping;

import com.kanwise.kanwise_service.controller.member.MemberController;
import com.kanwise.kanwise_service.controller.task.TaskController;
import com.kanwise.kanwise_service.model.task_status.TaskStatus;
import com.kanwise.kanwise_service.model.task_status.dto.TaskStatusDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class TaskStatusToTaskStatusDtoConverter implements Converter<TaskStatus, TaskStatusDto> {
    @Override
    public TaskStatusDto convert(MappingContext<TaskStatus, TaskStatusDto> mappingContext) {
        TaskStatus taskStatus = mappingContext.getSource();
        TaskStatusDto taskStatusDto = TaskStatusDto.builder()
                .id(taskStatus.getId())
                .label(taskStatus.getLabel())
                .setAt(taskStatus.getSetAt())
                .setTill(taskStatus.getSetTill())
                .taskId(taskStatus.getTask().getId())
                .setBy(taskStatus.getSetBy().getUsername())
                .ongoing(taskStatus.isOngoing())
                .build();

        addHateoasLinks(taskStatus, taskStatusDto);
        return taskStatusDto;
    }

    private void addHateoasLinks(TaskStatus taskStatus, TaskStatusDto taskStatusDto) {
        taskStatusDto.add(linkTo(methodOn(TaskController.class).findTask(taskStatus.getTask().getId())).withRel("task"));
        taskStatusDto.add(linkTo(methodOn(MemberController.class).findMemberByUsername(taskStatus.getSetBy().getUsername())).withRel("setBy"));
    }
}
