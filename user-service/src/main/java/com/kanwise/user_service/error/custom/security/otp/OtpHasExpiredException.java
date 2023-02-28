package com.kanwise.user_service.error.custom.security.otp;

public class OtpHasExpiredException extends RuntimeException {
    public OtpHasExpiredException() {
        super("OTP_HAS_EXPIRED");
    }
}
