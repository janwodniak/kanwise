package com.kanwise.user_service.service.authentication.password.implementation;

import com.kanwise.user_service.error.custom.security.password.token.InvalidPasswordResetTokenException;
import com.kanwise.user_service.model.authentication.password.ForgottenPasswordResetCommand;
import com.kanwise.user_service.model.authentication.password.PasswordResetCommand;
import com.kanwise.user_service.service.authentication.password.IPasswordValidationService;
import com.kanwise.user_service.service.password.encoder.implementation.PasswordEncoderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PasswordValidationService implements IPasswordValidationService {

    private final PasswordEncoderService passwordEncoderService;

    @Override
    public void validatePasswordResetCommand(PasswordResetCommand command, String currentPassword) {
        if (!passwordEncoderService.encodedPasswordMatches(command.currentPassword(), currentPassword)) {
            throw new InvalidPasswordResetTokenException("INVALID_CURRENT_PASSWORD");
        }

        if (passwordEncoderService.encodedPasswordMatches(command.newPassword(), currentPassword)) {
            throw new InvalidPasswordResetTokenException("NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_CURRENT_PASSWORD");
        }
    }

    @Override
    public void validateForgottenPasswordResetCommand(ForgottenPasswordResetCommand command, String currentPassword) {
        if (passwordEncoderService.encodedPasswordMatches(command.password(), currentPassword)) {
            throw new InvalidPasswordResetTokenException("NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_CURRENT_PASSWORD");
        }
    }
}
