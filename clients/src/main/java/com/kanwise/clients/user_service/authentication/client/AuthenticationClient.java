package com.kanwise.clients.user_service.authentication.client;

import com.kanwise.clients.user_service.authentication.model.OtpSmsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "user-service", path = "/auth")
public interface AuthenticationClient {

    @PostMapping("/otp/sms/response")
    ResponseEntity<HttpStatus> sendOtpSmsResponse(@RequestBody OtpSmsResponse otpSmsResponse);
}
