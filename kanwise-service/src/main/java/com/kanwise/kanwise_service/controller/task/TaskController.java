package com.kanwise.kanwise_service.controller.task;

import com.kanwise.kanwise_service.error.handling.ExceptionHandling;
import com.kanwise.kanwise_service.model.member.Member;
import com.kanwise.kanwise_service.model.member.dto.MemberDto;
import com.kanwise.kanwise_service.model.task.Task;
import com.kanwise.kanwise_service.model.task.command.CreateTaskCommand;
import com.kanwise.kanwise_service.model.task.command.EditTaskCommand;
import com.kanwise.kanwise_service.model.task.command.EditTaskPartiallyCommand;
import com.kanwise.kanwise_service.model.task.dto.TaskDto;
import com.kanwise.kanwise_service.service.task.ITaskService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kanwise.kanwise_service.model.http.HttpMethod.PATCH;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/task")
@RestController
public class TaskController extends ExceptionHandling {

    private final ITaskService taskService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Find task",
            notes = "This endpoint is used to find task.",
            response = TaskDto.class,
            responseReference = "ResponseEntity<TaskDto>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDto> findTask(@PathVariable("taskId") long taskId) {
        Task task = taskService.findTaskById(taskId);
        return new ResponseEntity<>(modelMapper.map(task, TaskDto.class), OK);
    }

    @ApiOperation(value = "Create task",
            notes = "This endpoint is used to create task.",
            response = TaskDto.class,
            responseReference = "ResponseEntity<TaskDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody @Valid CreateTaskCommand command) {
        Task task = taskService.createTask(modelMapper.map(command, Task.class));
        return new ResponseEntity<>(modelMapper.map(task, TaskDto.class), CREATED);
    }

    @ApiOperation(value = "Delete task",
            notes = "This endpoint is used to delete task.",
            response = TaskDto.class,
            responseReference = "ResponseEntity<TaskDto>",
            httpMethod = DELETE,
            produces = APPLICATION_JSON_VALUE)
    @DeleteMapping("/{taskId}")
    public ResponseEntity<HttpStatus> deleteTask(@PathVariable("taskId") long taskId) {
        taskService.deleteTask(taskId);
        return new ResponseEntity<>(NO_CONTENT);
    }

    @ApiOperation(value = "Edit task",
            notes = "This endpoint is used to edit task.",
            response = TaskDto.class,
            responseReference = "ResponseEntity<TaskDto>",
            httpMethod = PUT,
            produces = APPLICATION_JSON_VALUE)
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto> editTask(@PathVariable("taskId") long taskId, @RequestBody @Valid EditTaskCommand command) {
        Task task = taskService.editTask(taskId, command);
        return new ResponseEntity<>(modelMapper.map(task, TaskDto.class), OK);
    }

    @ApiOperation(value = "Edit task partially",
            notes = "This endpoint is used to edit task partially.",
            response = TaskDto.class,
            responseReference = "ResponseEntity<TaskDto>",
            httpMethod = PATCH,
            produces = APPLICATION_JSON_VALUE)
    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskDto> editTaskPartially(@PathVariable("taskId") long taskId, @RequestBody @Valid EditTaskPartiallyCommand command) {
        Task task = taskService.editTaskPartially(taskId, command);
        return new ResponseEntity<>(modelMapper.map(task, TaskDto.class), OK);
    }


    @ApiOperation(value = "Get members which are assigned to indicated task",
            notes = "This endpoint is used to get members which are assigned to indicated task.",
            response = MemberDto.class,
            responseReference = "ResponseEntity<Set<MemberDto>>",
            httpMethod = GET,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{taskId}/members")
    public ResponseEntity<Set<MemberDto>> findAssignedMembers(@PathVariable("taskId") long taskId) {
        Set<Member> assignedMembers = taskService.findAssignedMembers(taskId);
        return new ResponseEntity<>(assignedMembers.stream().map(member -> modelMapper.map(member, MemberDto.class)).collect(Collectors.toSet()), OK);
    }

    @ApiOperation(value = "Assign member to indicated task",
            notes = "This endpoint is used to assign member to indicated task.",
            response = MemberDto.class,
            responseReference = "ResponseEntity<Set<MemberDto>>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PostMapping(path = "/{taskId}/members/assign", params = "usernames")
    public ResponseEntity<Set<MemberDto>> assignMembersToTask(@PathVariable("taskId") long taskId, @RequestParam Set<String> usernames) {
        Set<Member> members = taskService.assignMembersToTask(taskId, usernames);
        return new ResponseEntity<>(members.stream().map(member -> modelMapper.map(member, MemberDto.class)).collect(Collectors.toSet()), OK);
    }
}
