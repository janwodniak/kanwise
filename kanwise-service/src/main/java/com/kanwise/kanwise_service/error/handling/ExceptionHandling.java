package com.kanwise.kanwise_service.error.handling;


import com.kanwise.kanwise_service.error.custom.exception.MissingRoleHeaderException;
import com.kanwise.kanwise_service.error.custom.exception.MissingUsernameHeaderException;
import com.kanwise.kanwise_service.error.custom.join.request.JoinRequestAlreadyRespondedException;
import com.kanwise.kanwise_service.error.custom.join.request.JoinRequestNotFoundException;
import com.kanwise.kanwise_service.error.custom.join.response.JoinResponseNotFoundException;
import com.kanwise.kanwise_service.error.custom.member.MemberAlreadyAssignedToProjectException;
import com.kanwise.kanwise_service.error.custom.member.MemberDoesNotBelongException;
import com.kanwise.kanwise_service.error.custom.member.MemberNotFoundException;
import com.kanwise.kanwise_service.error.custom.project.MemberNotAssignedToProjectException;
import com.kanwise.kanwise_service.error.custom.project.ProjectNotFoundException;
import com.kanwise.kanwise_service.error.custom.task.AlreadyReactedToComment;
import com.kanwise.kanwise_service.error.custom.task.MemberAlreadyAssignedToTaskException;
import com.kanwise.kanwise_service.error.custom.task.TaskCommentNotFoundException;
import com.kanwise.kanwise_service.error.custom.task.TaskNotFoundException;
import com.kanwise.kanwise_service.error.model.ValidationErrorDto;
import com.kanwise.kanwise_service.model.response.HttpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionHandling {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorDto>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exc) {
        return ResponseEntity.badRequest().body(
                exc.getFieldErrors().stream()
                        .map(fieldError -> new ValidationErrorDto(fieldError.getField(), fieldError.getDefaultMessage()))
                        .toList());
    }


    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timestamp(LocalDateTime.now())
                .httpStatusCode(httpStatus.value())
                .httpStatus(httpStatus)
                .reason(httpStatus.getReasonPhrase())
                .message(message)
                .build(), httpStatus);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<List<ValidationErrorDto>> handleMethodArgumentNotValidException(BindException exc) {
        return ResponseEntity.badRequest().body(
                exc.getFieldErrors().stream()
                        .map(fieldError -> new ValidationErrorDto(fieldError.getField(), fieldError.getDefaultMessage()))
                        .toList());
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<HttpResponse> projectNotFoundException(ProjectNotFoundException exception) {
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<HttpResponse> taskNotFoundException(TaskNotFoundException exception) {
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<HttpResponse> userNotFoundException(MemberNotFoundException exception) {
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(JoinRequestAlreadyRespondedException.class)
    public ResponseEntity<HttpResponse> joinRequestAlreadyRespondedException(JoinRequestAlreadyRespondedException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MemberAlreadyAssignedToProjectException.class)
    public ResponseEntity<HttpResponse> memberAlreadyBelongsException(MemberAlreadyAssignedToProjectException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MemberDoesNotBelongException.class)
    public ResponseEntity<HttpResponse> memberDoesNotBelongException(MemberDoesNotBelongException exception) {
        return createHttpResponse(FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(TaskCommentNotFoundException.class)
    public ResponseEntity<HttpResponse> taskCommentNotFoundException(TaskCommentNotFoundException exception) {
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(AlreadyReactedToComment.class)
    public ResponseEntity<HttpResponse> alreadyReactedToComment(AlreadyReactedToComment exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }


    @ExceptionHandler(MemberNotAssignedToProjectException.class)
    public ResponseEntity<HttpResponse> notProjectMemberException(MemberNotAssignedToProjectException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MemberAlreadyAssignedToTaskException.class)
    public ResponseEntity<HttpResponse> alreadyAssignedToTask(MemberAlreadyAssignedToTaskException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MissingUsernameHeaderException.class)
    public ResponseEntity<HttpResponse> missingUsernameHeaderException(MissingUsernameHeaderException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MissingRoleHeaderException.class)
    public ResponseEntity<HttpResponse> missingRoleHeaderException(MissingRoleHeaderException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(JoinRequestNotFoundException.class)
    public ResponseEntity<HttpResponse> joinRequestNotFoundException(JoinRequestNotFoundException exception) {
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(JoinResponseNotFoundException.class)
    public ResponseEntity<HttpResponse> joinResponseNotFoundException(JoinResponseNotFoundException exception) {
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }
}
