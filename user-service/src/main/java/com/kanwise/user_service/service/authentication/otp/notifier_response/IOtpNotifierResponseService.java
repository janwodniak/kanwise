package com.kanwise.user_service.service.authentication.otp.notifier_response;

import com.kanwise.user_service.model.otp.OtpNotifierResponse;

public interface IOtpNotifierResponseService<T extends OtpNotifierResponse> {
    void processOtpResponse(T otpNotifierResponse);
}
