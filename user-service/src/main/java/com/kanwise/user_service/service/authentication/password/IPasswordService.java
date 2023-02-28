package com.kanwise.user_service.service.authentication.password;

import com.kanwise.user_service.model.authentication.password.ForgottenPasswordResetCommand;
import com.kanwise.user_service.model.authentication.password.ForgottenPasswordResetRequest;
import com.kanwise.user_service.model.authentication.password.PasswordResetCommand;

public interface IPasswordService {
    void resetPassword(PasswordResetCommand command);

    void handleForgottenPasswordRequest(ForgottenPasswordResetRequest request);

    void resetForgottenPassword(ForgottenPasswordResetCommand command);
}
