package com.kanwise.user_service.service.authentication.password;

import com.kanwise.user_service.model.authentication.password.ForgottenPasswordResetCommand;
import com.kanwise.user_service.model.authentication.password.PasswordResetCommand;

public interface IPasswordValidationService {

    void validatePasswordResetCommand(PasswordResetCommand command, String currentPassword);

    void validateForgottenPasswordResetCommand(ForgottenPasswordResetCommand command, String currentPassword);
}
