package com.kanwise.notification_service.service.sms.otp;

import com.kanwise.clients.user_service.authentication.client.AuthenticationClient;
import com.kanwise.clients.user_service.authentication.model.OtpSmsResponse;
import com.kanwise.notification_service.service.sms.ISmsResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OtpSmsResponseService implements ISmsResponseService<OtpSmsResponse> {

    private final AuthenticationClient authenticationClient;

    @Override
    public void sendSmsResponse(OtpSmsResponse otpSmsResponse) {
        authenticationClient.sendOtpSmsResponse(otpSmsResponse);
    }
}
