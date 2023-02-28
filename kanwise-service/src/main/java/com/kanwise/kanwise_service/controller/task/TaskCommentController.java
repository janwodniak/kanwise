package com.kanwise.kanwise_service.controller.task;

import com.kanwise.kanwise_service.error.handling.ExceptionHandling;
import com.kanwise.kanwise_service.model.task_comment.TaskComment;
import com.kanwise.kanwise_service.model.task_comment.command.CreateTaskCommentCommand;
import com.kanwise.kanwise_service.model.task_comment.command.CreateTaskCommentPageCommand;
import com.kanwise.kanwise_service.model.task_comment.dto.TaskCommentDto;
import com.kanwise.kanwise_service.model.task_comment_reaction.TaskCommentReaction;
import com.kanwise.kanwise_service.model.task_comment_reaction.command.CreateTaskCommentReactionCommand;
import com.kanwise.kanwise_service.model.task_comment_reaction.dto.TaskCommentReactionDto;
import com.kanwise.kanwise_service.service.task.comment.ITaskCommentReactionService;
import com.kanwise.kanwise_service.service.task.comment.ITaskCommentService;
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

import static javax.ws.rs.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/task")
@RestController
public class TaskCommentController extends ExceptionHandling {

    private final ITaskCommentService taskCommentService;
    private final ITaskCommentReactionService taskCommentReactionService;
    private final ModelMapper modelMapper;

    @ApiOperation(value = "Create a new task comment",
            notes = "Create a new task comment",
            response = TaskCommentDto.class,
            responseReference = "ResponseEntity<TaskCommentDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PostMapping("/comment")
    public ResponseEntity<TaskCommentDto> createTaskComment(@RequestBody @Valid CreateTaskCommentCommand command) {
        TaskComment taskComment = taskCommentService.saveTaskComment(modelMapper.map(command, TaskComment.class));
        return new ResponseEntity<>(modelMapper.map(taskComment, TaskCommentDto.class), CREATED);
    }

    @ApiOperation(value = "Get tasks",
            notes = "Get tasks",
            response = TaskCommentDto.class,
            responseReference = "ResponseEntity<Page<TaskCommentDto>>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @GetMapping("/{taskId}/comments")
    public ResponseEntity<Page<TaskCommentDto>> findTaskCommentsForTask(@PathVariable("taskId") long id, @Valid CreateTaskCommentPageCommand command) {
        Page<TaskComment> taskComments = taskCommentService.getTaskCommentsByTaskId(id, modelMapper.map(command, Pageable.class));
        return new ResponseEntity<>(taskComments.map(taskComment -> modelMapper.map(taskComment, TaskCommentDto.class)), OK);
    }

    @ApiOperation(value = "Create a new task comment reaction",
            notes = "Create a new task comment reaction",
            response = TaskCommentReactionDto.class,
            responseReference = "ResponseEntity<TaskCommentReactionDto>",
            httpMethod = POST,
            produces = APPLICATION_JSON_VALUE)
    @PostMapping("/comment/reaction")
    public ResponseEntity<TaskCommentReactionDto> createTaskCommentReaction(@RequestBody @Valid CreateTaskCommentReactionCommand command) {
        TaskCommentReaction taskCommentReaction = taskCommentReactionService.saveTaskCommentReaction(modelMapper.map(command, TaskCommentReaction.class));
        return new ResponseEntity<>(modelMapper.map(taskCommentReaction, TaskCommentReactionDto.class), CREATED);
    }
}
