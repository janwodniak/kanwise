package com.kanwise.user_service.service.password.token.implementation;


import com.kanwise.user_service.configuration.security.password_reset_token.PasswordResetTokenConfigurationProperties;
import com.kanwise.user_service.error.custom.security.password.token.PasswordResetTokenExceptionNotFoundException;
import com.kanwise.user_service.model.token.PasswordResetToken;
import com.kanwise.user_service.repository.authentication.PasswordResetTokenRepository;
import com.kanwise.user_service.service.password.token.IPasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService implements IPasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordResetTokenConfigurationProperties passwordResetTokenConfigurationProperties;

    private final Clock clock;

    public PasswordResetToken getPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .orElseThrow(PasswordResetTokenExceptionNotFoundException::new);
    }

    @Transactional
    public void savePasswordResetToken(PasswordResetToken passwordResetToken) {
        passwordResetTokenRepository.save(passwordResetToken);
    }

    public PasswordResetToken generatePasswordResetToken() {
        return PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .createdAt(now(clock))
                .expiresAt(now(clock).plus(passwordResetTokenConfigurationProperties.expiration()))
                .build();
    }

    @Transactional
    public void setStatus(PasswordResetToken token) {
        passwordResetTokenRepository.updateConfirmedAt(now(clock), token.getToken());
    }
}

