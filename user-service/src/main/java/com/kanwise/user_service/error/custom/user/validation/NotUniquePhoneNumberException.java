package com.kanwise.user_service.error.custom.user.validation;

public class NotUniquePhoneNumberException extends RuntimeException {
    public NotUniquePhoneNumberException() {
        super("PHONE_NUMBER_NOT_UNIQUE");
    }
}
