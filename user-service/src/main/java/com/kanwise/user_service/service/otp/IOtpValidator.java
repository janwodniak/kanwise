package com.kanwise.user_service.service.otp;

import com.kanwise.user_service.model.otp.OneTimePassword;

public interface IOtpValidator {
    void validateOtp(OneTimePassword oneTimePassword, String code);
}
