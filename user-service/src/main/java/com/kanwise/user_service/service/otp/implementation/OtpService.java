package com.kanwise.user_service.service.otp.implementation;

import com.kanwise.clients.user_service.authentication.model.OtpStatus;
import com.kanwise.user_service.configuration.security.otp.OtpConfigurationProperties;
import com.kanwise.user_service.error.custom.security.otp.OtpNotFoundException;
import com.kanwise.user_service.model.otp.OneTimePassword;
import com.kanwise.user_service.repository.authentication.OneTimePasswordRepository;
import com.kanwise.user_service.service.otp.IOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;

import static com.kanwise.clients.user_service.authentication.model.OtpStatus.CONFIRMED;
import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
public class OtpService implements IOtpService {

    private final OneTimePasswordRepository oneTimePasswordRepository;
    private final OtpConfigurationProperties otpConfigurationProperties;
    private final Clock clock;

    @Transactional(readOnly = true)
    public OneTimePassword getOneTimePasswordById(long id) {
        return oneTimePasswordRepository.findById(id).orElseThrow(() -> new OtpNotFoundException(id));
    }

    @Transactional
    public OneTimePassword saveOneTimePassword(OneTimePassword oneTimePassword) {
        return oneTimePasswordRepository.save(oneTimePassword);
    }

    @Transactional
    @Override
    public void confirmOtp(OneTimePassword oneTimePassword) {
        oneTimePassword.setStatus(CONFIRMED);
        oneTimePassword.setConfirmedAt(now());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsById(long id) {
        return oneTimePasswordRepository.existsById(id);
    }

    private String generateOneTimePasswordCode() {
        return new SecureRandom().ints(otpConfigurationProperties.length(), 0, 10)
                .mapToObj(String::valueOf)
                .reduce("", String::concat);
    }

    public OneTimePassword generateOneTimePassword() {
        return OneTimePassword.builder()
                .code(generateOneTimePasswordCode())
                .status(OtpStatus.CREATED)
                .createdAt(now(clock))
                .expiresAt(now(clock).plus(otpConfigurationProperties.expiration()))
                .build();
    }

    @Transactional
    public void updateOneTimePasswordStatus(OtpStatus status, long id) {
        oneTimePasswordRepository.findById(id).ifPresentOrElse(otp -> otp.setStatus(status), () -> {
            throw new OtpNotFoundException(id);
        });
    }
}

