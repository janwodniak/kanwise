package com.kanwise.user_service.service.authentication.otp.notifier_response.implementation.sms;

import com.kanwise.user_service.model.otp.OtpSmsNotifierResponse;
import com.kanwise.user_service.service.authentication.otp.notifier_response.IOtpNotifierResponseService;
import com.kanwise.user_service.service.otp.IOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OtpSmsNotifierResponseService implements IOtpNotifierResponseService<OtpSmsNotifierResponse> {
    private final IOtpService otpService;

    @Override
    public void processOtpResponse(OtpSmsNotifierResponse otpNotifierResponse) {
        otpService.updateOneTimePasswordStatus(otpNotifierResponse.getStatus(), otpNotifierResponse.getOtpId());
    }
}
