package com.kanwise.user_service.service.password.token;

import com.kanwise.user_service.model.token.PasswordResetToken;

public interface IPasswordResetTokenValidator {

    void validate(PasswordResetToken passwordResetToken);

}
