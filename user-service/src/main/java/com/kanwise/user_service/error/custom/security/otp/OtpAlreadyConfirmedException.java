package com.kanwise.user_service.error.custom.security.otp;

public class OtpAlreadyConfirmedException extends RuntimeException {
    public OtpAlreadyConfirmedException() {
        super("OTP_ALREADY_CONFIRMED");
    }
}
