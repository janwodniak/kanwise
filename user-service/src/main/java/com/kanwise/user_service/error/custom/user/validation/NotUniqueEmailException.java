package com.kanwise.user_service.error.custom.user.validation;

public class NotUniqueEmailException extends RuntimeException {
    public NotUniqueEmailException() {
        super("EMAIL_NOT_UNIQUE");
    }
}
