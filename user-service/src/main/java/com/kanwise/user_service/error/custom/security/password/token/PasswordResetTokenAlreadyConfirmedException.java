package com.kanwise.user_service.error.custom.security.password.token;

public class PasswordResetTokenAlreadyConfirmedException extends RuntimeException {
    public PasswordResetTokenAlreadyConfirmedException() {
        super("PASSWORD_RESET_TOKEN_ALREADY_CONFIRMED");
    }
}
