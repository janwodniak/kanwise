package com.kanwise.user_service.service.authentication.register.implementation;

import com.kanwise.user_service.model.authentication.response.RegisterResponse;
import com.kanwise.user_service.model.notification.email.EmailRequest;
import com.kanwise.user_service.model.notification.sms.OtpSmsRequest;
import com.kanwise.user_service.model.otp.OneTimePassword;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.service.authentication.register.IRegisterService;
import com.kanwise.user_service.service.notification.email.IEmailNotificationService;
import com.kanwise.user_service.service.notification.sms.ISmsNotificationService;
import com.kanwise.user_service.service.otp.IOtpService;
import com.kanwise.user_service.service.password.generator.IPasswordGeneratorService;
import com.kanwise.user_service.service.user.IUserService;
import com.netflix.discovery.shared.Pair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static com.kanwise.user_service.model.notification.email.EmailMessageType.ACCOUNT_CREATED;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;


@RequiredArgsConstructor
@Service
public class RegisterService implements IRegisterService {

    private final IUserService userService;
    private final IOtpService otpService;
    private final ISmsNotificationService<OtpSmsRequest> otpSmsNotificationService;

    private final IPasswordGeneratorService passwordGeneratorService;

    private final IEmailNotificationService<EmailRequest> emailNotificationService;


    @Transactional
    @Override
    public RegisterResponse register(User user) {
        User savedUser = userService.createUser(user, false, false);
        Optional<OneTimePassword> oneTimePassword = handleTwoFactorAuthenticationForUser(user);
        return RegisterResponse.builder().user(savedUser).oneTimePassword(oneTimePassword).build();
    }

    private Optional<OneTimePassword> handleTwoFactorAuthenticationForUser(User user) {
        if (user.isTwoFactorEnabled()) {
            return ofNullable(generateOneTimePasswordAndSendSmsNotification(user));
        } else {
            generatePasswordAndSendPasswordEmail(user);
            return empty();
        }
    }

    private OneTimePassword generateOneTimePasswordAndSendSmsNotification(User user) {
        OneTimePassword otp = getOneTimePasswordForUser(user);
        sendOneTimePasswordSmsNotification(user, otp);
        return otp;
    }

    private OneTimePassword getOneTimePasswordForUser(User user) {
        OneTimePassword oneTimePassword = otpService.generateOneTimePassword();
        user.addOneTimePassword(oneTimePassword);
        return otpService.saveOneTimePassword(oneTimePassword);
    }

    private void sendOneTimePasswordSmsNotification(User user, OneTimePassword oneTimePassword) {
        otpSmsNotificationService.sendSms(OtpSmsRequest.builder()
                .otpId(oneTimePassword.getId())
                .phoneNumber(user.getPhoneNumber())
                .content(oneTimePassword.getCode())
                .build());
    }

    private void generatePasswordAndSendPasswordEmail(User user) {
        Pair<String, String> passwordAndEncryptedPassword = passwordGeneratorService.generatePasswordAndEncryptedPassword();
        user.setPassword(passwordAndEncryptedPassword.second());
        sendPasswordEmail(user, passwordAndEncryptedPassword.first());
    }

    private void sendPasswordEmail(User user, String password) {
        emailNotificationService.sendEmail(EmailRequest.builder()
                .to(user.getEmail())
                .subject("Account created 🥳")
                .isHtml(true)
                .type(ACCOUNT_CREATED)
                .data(Map.of("password", password, "firstName", user.getFirstName()))
                .build());
    }
}
