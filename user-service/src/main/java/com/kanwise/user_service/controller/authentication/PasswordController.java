package com.kanwise.user_service.controller.authentication;

import com.kanwise.user_service.model.authentication.password.ForgottenPasswordResetCommand;
import com.kanwise.user_service.model.authentication.password.ForgottenPasswordResetRequest;
import com.kanwise.user_service.model.authentication.password.PasswordResetCommand;
import com.kanwise.user_service.service.authentication.password.IPasswordService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RequestMapping("/auth/password")
@RestController
public class PasswordController {

    private final IPasswordService passwordService;

    @ApiOperation(value = "Reset password",
            notes = "This endpoint is used to reset password.",
            response = HttpStatus.class,
            responseReference = "ResponseEntity<HttpStatus>",
            httpMethod = "POST",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("@authenticationFacade.isUserByIdAndHasAuthority(#command.userId(),'PASSWORD_WRITE') or @authenticationFacade.admin")
    @PostMapping("/reset")
    public ResponseEntity<HttpStatus> resetPassword(@RequestBody @Valid PasswordResetCommand command) {
        passwordService.resetPassword(command);
        return new ResponseEntity<>(OK);
    }

    @ApiOperation(value = "Forgotten password reset",
            notes = "This endpoint is used to reset forgotten password.",
            response = HttpStatus.class,
            responseReference = "ResponseEntity<HttpStatus>",
            httpMethod = "POST",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping("/reset/forgotten")
    public ResponseEntity<HttpStatus> resetForgottenPassword(@RequestBody @Valid ForgottenPasswordResetCommand command) {
        passwordService.resetForgottenPassword(command);
        return new ResponseEntity<>(OK);
    }

    @ApiOperation(value = "Forgotten password reset request",
            notes = "This endpoint is used to request forgotten password reset. If request is successful, reset token will be sent to user's email.",
            response = HttpStatus.class,
            responseReference = "ResponseEntity<HttpStatus>",
            httpMethod = "POST",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping("/request/forgotten")
    public ResponseEntity<HttpStatus> handleForgottenPasswordRequest(@RequestBody @Valid ForgottenPasswordResetRequest request) {
        passwordService.handleForgottenPasswordRequest(request);
        return new ResponseEntity<>(OK);
    }
}
