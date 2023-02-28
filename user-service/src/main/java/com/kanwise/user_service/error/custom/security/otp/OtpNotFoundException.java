package com.kanwise.user_service.error.custom.security.otp;

public class OtpNotFoundException extends RuntimeException {
    public OtpNotFoundException(long id) {
        super("OTP_WITH_ID_%s_NOT_FOUND".formatted(id));
    }
}

