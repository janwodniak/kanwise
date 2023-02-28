package com.kanwise.user_service.service.authentication.password.implementation;

import com.kanwise.user_service.model.authentication.password.ForgottenPasswordResetCommand;
import com.kanwise.user_service.model.authentication.password.ForgottenPasswordResetRequest;
import com.kanwise.user_service.model.authentication.password.PasswordResetCommand;
import com.kanwise.user_service.model.notification.email.EmailMessageType;
import com.kanwise.user_service.model.notification.email.EmailRequest;
import com.kanwise.user_service.model.token.PasswordResetToken;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.service.authentication.password.IPasswordService;
import com.kanwise.user_service.service.authentication.password.IPasswordValidationService;
import com.kanwise.user_service.service.notification.email.IEmailNotificationService;
import com.kanwise.user_service.service.password.encoder.IPasswordEncoderService;
import com.kanwise.user_service.service.password.token.IPasswordResetTokenService;
import com.kanwise.user_service.service.password.token.IPasswordResetTokenValidator;
import com.kanwise.user_service.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class PasswordService implements IPasswordService {

    private final IPasswordResetTokenService passwordResetTokenService;
    private final IPasswordResetTokenValidator passwordResetTokenValidator;
    private final IUserService userService;
    private final IEmailNotificationService<EmailRequest> emailNotificationService;
    private final IPasswordEncoderService passwordEncoderService;
    private final IPasswordValidationService passwordValidationService;

    @Transactional
    @Override
    public void resetPassword(PasswordResetCommand command) {
        User user = userService.findUserById(command.userId());
        passwordValidationService.validatePasswordResetCommand(command, user.getPassword());
        user.setPassword(passwordEncoderService.encodePassword(command.newPassword()));
    }

    @Transactional
    @Override
    public void resetForgottenPassword(ForgottenPasswordResetCommand command) {
        PasswordResetToken passwordResetToken = confirmTokenValidation(command.token());
        User user = userService.findUserById(passwordResetToken.getUser().getId());
        passwordValidationService.validateForgottenPasswordResetCommand(command, user.getPassword());
        user.setPassword(passwordEncoderService.encodePassword(command.password()));
    }

    @Transactional
    @Override
    public void handleForgottenPasswordRequest(ForgottenPasswordResetRequest request) {
        User user = userService.findUserByEmail(request.email());
        generatePasswordResetTokenAndSendResetPasswordEmail(user);
    }

    private PasswordResetToken confirmTokenValidation(String token) {
        PasswordResetToken confirmationToken = passwordResetTokenService.getPasswordResetToken(token);
        passwordResetTokenValidator.validate(confirmationToken);
        passwordResetTokenService.setStatus(confirmationToken);
        return confirmationToken;
    }

    private void generatePasswordResetTokenAndSendResetPasswordEmail(User user) {
        sendResetPasswordEmail(user, getPasswordResetTokenForUser(user));
    }

    private PasswordResetToken getPasswordResetTokenForUser(User user) {
        PasswordResetToken resetToken = passwordResetTokenService.generatePasswordResetToken();
        user.addPasswordResetToken(resetToken);
        passwordResetTokenService.savePasswordResetToken(resetToken);
        return resetToken;
    }

    private void sendResetPasswordEmail(User user, PasswordResetToken resetToken) {
        String link = "http://localhost:4200//password/reset?token=" + resetToken.getToken();
        emailNotificationService.sendEmail(EmailRequest.builder()
                .to(user.getEmail())
                .subject("Reset password 🔑")
                .type(EmailMessageType.PASSWORD_RESET)
                .isHtml(true)
                .data(Map.of("resetPasswordUrl", link, "firstName", user.getFirstName()))
                .build());
    }
}
