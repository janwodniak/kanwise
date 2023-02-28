package com.kanwise.user_service.controller.authentication;

import com.kanwise.user_service.model.otp.OtpValidationRequest;
import com.kanwise.user_service.service.authentication.otp.validation.IOtpValidationService;
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
@RequestMapping("/auth")
@RestController
public class OtpValidationController {

    private final IOtpValidationService<OtpValidationRequest> registrationOtpValidationService;

    @ApiOperation(value = "Validate OTP SMS",
            notes = "This endpoint is used to validate OTP SMS.",
            response = HttpStatus.class,
            responseReference = "ResponseEntity<HttpStatus>",
            httpMethod = "POST",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @PostMapping("/registration/otp/sms")
    public ResponseEntity<HttpStatus> validateRegistrationOtpSms(@RequestBody @Valid OtpValidationRequest otpValidationRequest) {
        registrationOtpValidationService.validateOtp(otpValidationRequest);
        return new ResponseEntity<>(OK);
    }
}
