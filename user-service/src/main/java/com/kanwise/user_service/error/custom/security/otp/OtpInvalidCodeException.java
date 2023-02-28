package com.kanwise.user_service.error.custom.security.otp;

public class OtpInvalidCodeException extends RuntimeException {
    public OtpInvalidCodeException() {
        super("OTP_INVALID_CODE");
    }
}
