package com.kanwise.user_service.service.password.token.implementation;

import com.kanwise.user_service.error.custom.security.password.token.PasswordResetTokenAlreadyConfirmedException;
import com.kanwise.user_service.error.custom.security.password.token.PasswordResetTokenExpiredException;
import com.kanwise.user_service.model.token.PasswordResetToken;
import com.kanwise.user_service.service.password.token.IPasswordResetTokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;

@RequiredArgsConstructor
@Service
public class PasswordResetTokenValidator implements IPasswordResetTokenValidator {

    private final Clock clock;

    @Override
    public void validate(PasswordResetToken passwordResetToken) {
        if (passwordResetToken.isConfirmed()) {
            throw new PasswordResetTokenAlreadyConfirmedException();
        }

        if (passwordResetToken.isExpired(clock)) {
            throw new PasswordResetTokenExpiredException();
        }
    }
}
