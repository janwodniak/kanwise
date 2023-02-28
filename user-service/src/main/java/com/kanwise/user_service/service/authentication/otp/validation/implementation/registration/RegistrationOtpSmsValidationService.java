package com.kanwise.user_service.service.authentication.otp.validation.implementation.registration;

import com.kanwise.user_service.model.notification.email.EmailRequest;
import com.kanwise.user_service.model.otp.OneTimePassword;
import com.kanwise.user_service.model.otp.OtpValidationRequest;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.service.authentication.otp.validation.IOtpValidationService;
import com.kanwise.user_service.service.notification.email.IEmailNotificationService;
import com.kanwise.user_service.service.otp.IOtpService;
import com.kanwise.user_service.service.otp.IOtpValidator;
import com.kanwise.user_service.service.password.generator.IPasswordGeneratorService;
import com.netflix.discovery.shared.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kanwise.user_service.model.notification.email.EmailMessageType.ACCOUNT_CREATED;
import static java.util.Map.of;

@Slf4j
@RequiredArgsConstructor
@Service
public class RegistrationOtpSmsValidationService implements IOtpValidationService<OtpValidationRequest> {

    private final IOtpService otpService;
    private final IOtpValidator otpValidator;
    private final IEmailNotificationService<EmailRequest> emailNotificationService;
    private final IPasswordGeneratorService passwordGeneratorService;

    @Transactional
    @Override
    public void validateOtp(OtpValidationRequest otpValidationRequest) {
        OneTimePassword oneTimePassword = otpService.getOneTimePasswordById(otpValidationRequest.getOtpId());
        otpValidator.validateOtp(oneTimePassword, otpValidationRequest.getCode());
        otpService.confirmOtp(oneTimePassword);

        Pair<String, String> passwordAndEncryptedPassword = passwordGeneratorService.generatePasswordAndEncryptedPassword();

        User user = oneTimePassword.getUser();
        user.setPassword(passwordAndEncryptedPassword.second());
        user.setEnabled(true);

        sendAccountCreatedEmail(user, passwordAndEncryptedPassword.first());
    }

    private void sendAccountCreatedEmail(User user, String password) {
        emailNotificationService.sendEmail(EmailRequest.builder()
                .to(user.getEmail())
                .subject("Account created 🥳")
                .type(ACCOUNT_CREATED)
                .isHtml(true)
                .data(of("password", password, "firstName", user.getFirstName()))
                .build());
    }
}
