package com.kanwise.user_service.error.handling;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.kanwise.user_service.error.custom.security.otp.OtpAlreadyConfirmedException;
import com.kanwise.user_service.error.custom.security.otp.OtpHasExpiredException;
import com.kanwise.user_service.error.custom.security.otp.OtpInvalidCodeException;
import com.kanwise.user_service.error.custom.security.otp.OtpNotDeliveredException;
import com.kanwise.user_service.error.custom.security.otp.OtpNotFoundException;
import com.kanwise.user_service.error.custom.security.password.token.InvalidPasswordResetTokenException;
import com.kanwise.user_service.error.custom.security.password.token.PasswordResetTokenAlreadyConfirmedException;
import com.kanwise.user_service.error.custom.security.password.token.PasswordResetTokenExceptionNotFoundException;
import com.kanwise.user_service.error.custom.security.password.token.PasswordResetTokenExpiredException;
import com.kanwise.user_service.error.custom.user.ImageNotFoundException;
import com.kanwise.user_service.error.custom.user.UserIsDisabledException;
import com.kanwise.user_service.error.custom.user.UserNotFoundException;
import com.kanwise.user_service.error.custom.user.validation.NotUniqueEmailException;
import com.kanwise.user_service.error.custom.user.validation.NotUniquePhoneNumberException;
import com.kanwise.user_service.error.custom.user.validation.NotUniqueUsernameException;
import com.kanwise.user_service.model.error.ValidationErrorDto;
import com.kanwise.user_service.model.response.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestControllerAdvice
public class ExceptionHandling implements MessageFormatter {

    private static ValidationErrorDto generateValidationErrorDto(FieldError fieldError) {
        return ValidationErrorDto.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorDto>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exc) {
        return ResponseEntity.badRequest().body(
                exc.getFieldErrors().stream()
                        .map(ExceptionHandling::generateValidationErrorDto)
                        .toList()
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<List<ValidationErrorDto>> handleMethodArgumentNotValidException(BindException exc) {
        return ResponseEntity.badRequest().body(
                exc.getFieldErrors().stream()
                        .map(ExceptionHandling::generateValidationErrorDto)
                        .toList()
        );
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException exception) {
        return createHttpResponse(UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<HttpResponse> jwtVerificationException(JWTVerificationException exception) {
        return createHttpResponse(UNAUTHORIZED, formatMessage(exception.getMessage(), false));
    }

    @ExceptionHandler(UserIsDisabledException.class)
    public ResponseEntity<HttpResponse> userIsDisabledException(UserIsDisabledException exception) {
        return createHttpResponse(FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<HttpResponse> invalidPasswordResetTokenException(InvalidPasswordResetTokenException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(PasswordResetTokenExceptionNotFoundException.class)
    public ResponseEntity<HttpResponse> passwordResetTokenExceptionNotFoundException(PasswordResetTokenExceptionNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(PasswordResetTokenExpiredException.class)
    public ResponseEntity<HttpResponse> passwordResetTokenExpiredException(PasswordResetTokenExpiredException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(PasswordResetTokenAlreadyConfirmedException.class)
    public ResponseEntity<HttpResponse> passwordResetTokenAlreadyConfirmedException(PasswordResetTokenAlreadyConfirmedException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(OtpAlreadyConfirmedException.class)
    public ResponseEntity<HttpResponse> otpAlreadyConfirmedException(OtpAlreadyConfirmedException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(OtpHasExpiredException.class)
    public ResponseEntity<HttpResponse> otpHasExpiredException(OtpHasExpiredException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(OtpInvalidCodeException.class)
    public ResponseEntity<HttpResponse> otpInvalidCodeException(OtpInvalidCodeException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(OtpNotDeliveredException.class)
    public ResponseEntity<HttpResponse> otpNotDeliveredException(OtpNotDeliveredException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NotUniqueEmailException.class)
    public ResponseEntity<HttpResponse> notUniqueEmailException(NotUniqueEmailException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NotUniqueUsernameException.class)
    public ResponseEntity<HttpResponse> notUniqueUsernameException(NotUniqueUsernameException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NotUniquePhoneNumberException.class)
    public ResponseEntity<HttpResponse> notUniquePhoneNumberException(NotUniquePhoneNumberException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<HttpResponse> imageNotFoundException(ImageNotFoundException exception) {
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(OtpNotFoundException.class)
    public ResponseEntity<HttpResponse> otpNotFoundException(OtpNotFoundException exception) {
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException exception) {
        return createHttpResponse(NOT_FOUND, exception.getMessage());
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
}
