package com.kanwise.user_service.error.custom.security.password.token;

public class PasswordResetTokenExceptionNotFoundException extends RuntimeException {
    public PasswordResetTokenExceptionNotFoundException() {
        super("PASSWORD_RESET_TOKEN_NOT_FOUND");
    }
}
