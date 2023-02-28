package com.kanwise.user_service.controller.authentication;

import com.kanwise.user_service.error.handling.ExceptionHandling;
import com.kanwise.user_service.model.otp.OtpSmsNotifierResponse;
import com.kanwise.user_service.service.authentication.otp.notifier_response.IOtpNotifierResponseService;
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
@RequestMapping("/auth/otp")
@RestController
public class OtpNotifierResponseController extends ExceptionHandling {

    private final IOtpNotifierResponseService<OtpSmsNotifierResponse> otpSmsNotifierResponseService;

    @ApiOperation(value = "Consume OTP SMS response",
            notes = "This endpoint is used to consume OTP SMS responses from external services.",
            response = HttpStatus.class,
            responseReference = "ResponseEntity<HttpStatus>",
            httpMethod = "POST",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping("/sms/response")
    public ResponseEntity<HttpStatus> processOtpSmsResponse(@RequestBody @Valid OtpSmsNotifierResponse otpSmsNotifierResponse) {
        otpSmsNotifierResponseService.processOtpResponse(otpSmsNotifierResponse);
        return new ResponseEntity<>(OK);
    }
}
