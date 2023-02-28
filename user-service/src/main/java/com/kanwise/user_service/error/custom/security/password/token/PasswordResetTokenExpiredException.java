package com.kanwise.user_service.error.custom.security.password.token;

public class PasswordResetTokenExpiredException extends RuntimeException {
    public PasswordResetTokenExpiredException() {
        super("PASSWORD_RESET_TOKEN_EXPIRED");
    }
}
