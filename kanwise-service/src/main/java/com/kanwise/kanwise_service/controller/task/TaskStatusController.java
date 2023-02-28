package com.kanwise.kanwise_service.controller.task;

import com.kanwise.kanwise_service.error.handling.ExceptionHandling;
import com.kanwise.kanwise_service.model.task_status.TaskStatus;
import com.kanwise.kanwise_service.model.task_status.command.CreateTaskStatusCommand;
import com.kanwise.kanwise_service.model.task_status.command.CreateTaskStatusPageCommand;
import com.kanwise.kanwise_service.model.task_status.dto.TaskStatusDto;
import com.kanwise.kanwise_service.service.task.status.ITaskStatusService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/task")
@RestController
public class TaskStatusController extends ExceptionHandling {

    private final ITaskStatusService taskStatusService;
    private final ModelMapper modelMapper;


    @ApiOperation(value = "Get task statues",
            notes = "Get task statues",
            response = TaskStatusDto.class,
            responseReference = "ResponseEntity<Page<TaskStatusDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{taskId}/statuses")
    public ResponseEntity<Page<TaskStatusDto>> findTaskStatuses(@PathVariable("taskId") long taskId, @Valid CreateTaskStatusPageCommand command) {
        Page<TaskStatus> taskStatuses = taskStatusService.getTaskStatusByTaskId(taskId, modelMapper.map(command, Pageable.class));
        return new ResponseEntity<>(taskStatuses.map(taskStatus -> modelMapper.map(taskStatus, TaskStatusDto.class)), OK);
    }

    @ApiOperation(value = "Create task status",
            notes = "Create task status",
            response = TaskStatusDto.class,
            responseReference = "ResponseEntity<TaskStatusDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PostMapping("/status")
    public ResponseEntity<TaskStatusDto> createTaskStatus(@RequestBody @Valid CreateTaskStatusCommand command) {
        TaskStatus taskStatus = taskStatusService.createTaskStatus(modelMapper.map(command, TaskStatus.class));
        return new ResponseEntity<>(modelMapper.map(taskStatus, TaskStatusDto.class), CREATED);
    }
}
