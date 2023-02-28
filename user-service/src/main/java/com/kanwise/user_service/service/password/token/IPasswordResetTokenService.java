package com.kanwise.user_service.service.password.token;

import com.kanwise.user_service.model.token.PasswordResetToken;

public interface IPasswordResetTokenService {

    PasswordResetToken getPasswordResetToken(String token);

    PasswordResetToken generatePasswordResetToken();

    void savePasswordResetToken(PasswordResetToken resetToken);

    void setStatus(PasswordResetToken passwordResetToken);
}
