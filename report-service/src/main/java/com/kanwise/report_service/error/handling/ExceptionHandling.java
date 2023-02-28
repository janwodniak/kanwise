package com.kanwise.report_service.error.handling;


import com.kanwise.report_service.error.job.common.JobNotFoundException;
import com.kanwise.report_service.error.model.ValidationErrorDto;
import com.kanwise.report_service.error.subscriber.SubscriberNotFoundException;
import com.kanwise.report_service.model.response.HttpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

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

    @ExceptionHandler(SubscriberNotFoundException.class)
    public ResponseEntity<HttpResponse> handleSubscriberNotFoundException(SubscriberNotFoundException exc) {
        return createHttpResponse(NOT_FOUND, exc.getMessage());
    }

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<HttpResponse> handleJobNotFoundException(JobNotFoundException exc) {
        return createHttpResponse(NOT_FOUND, exc.getMessage());
    }
}
