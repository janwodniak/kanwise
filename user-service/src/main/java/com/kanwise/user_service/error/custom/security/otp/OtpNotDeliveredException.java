package com.kanwise.user_service.error.custom.security.otp;

public class OtpNotDeliveredException extends RuntimeException {
    public OtpNotDeliveredException() {
        super("OTP_NOT_DELIVERED");
    }
}
