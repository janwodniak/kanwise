package com.kanwise.user_service.error.custom.user.validation;

public class NotUniqueUsernameException extends RuntimeException {
    public NotUniqueUsernameException() {
        super("USERNAME_NOT_UNIQUE");
    }
}
