package com.kanwise.user_service.service.authentication.login.implementation;

import com.google.common.cache.LoadingCache;
import com.kanwise.user_service.configuration.security.brute_force_attack.BruteForceAttackConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith({OutputCaptureExtension.class, MockitoExtension.class})
class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;
    @Mock
    private BruteForceAttackConfigurationProperties bruteForceAttackConfigurationProperties;
    @Mock
    private LoadingCache<String, Integer> loginAttemptCache;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService(bruteForceAttackConfigurationProperties, loginAttemptCache);
    }

    @Test
    void shouldLogErrorIfUsernameIsNotFundInCache(CapturedOutput output) throws ExecutionException {
        // Given
        String nonExistingUsername = "nonExistingUsername";
        String expectedErrorMessage = "Username not found in cache";
        when(loginAttemptCache.get(nonExistingUsername)).thenThrow(NullPointerException.class);
        // When
        loginAttemptService.addUserToLoginAttemptCache(nonExistingUsername);
        // Then
        assertTrue(output.getOut().contains(expectedErrorMessage));
    }

    @Test
    void shouldLogErrorIfUsernameIsNotFoundInCacheDuringMaxAttemptsCheck(CapturedOutput output) throws ExecutionException {
        // Given
        String nonExistingUsername = "nonExistingUsername";
        String expectedErrorMessage = "Username not found in cache";
        when(loginAttemptCache.get(nonExistingUsername)).thenThrow(NullPointerException.class);
        // When
        loginAttemptService.hasExceedsMaxAttempts(nonExistingUsername);
        // Then
        assertTrue(output.getOut().contains(expectedErrorMessage));
    }
}