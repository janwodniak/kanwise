package com.kanwise.user_service.error.custom.user;

public class UserIsDisabledException extends RuntimeException {
    public UserIsDisabledException() {
        super("USER_IS_DISABLED");
    }
}
