package com.kanwise.user_service.error.custom.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("USER_NOT_FOUND");
    }
}

