package com.kanwise.user_service.service.authentication.login.implementation;

import com.google.common.cache.LoadingCache;
import com.kanwise.user_service.configuration.security.brute_force_attack.BruteForceAttackConfigurationProperties;
import com.kanwise.user_service.service.authentication.login.ILoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoginAttemptService implements ILoginAttemptService {

    private final BruteForceAttackConfigurationProperties bruteForceAttackConfigurationProperties;
    private final LoadingCache<String, Integer> loginAttemptCache;

    @Override
    public void evictUserFromLoginAttemptCache(String username) {
        loginAttemptCache.invalidate(username);
    }

    @Override
    public void addUserToLoginAttemptCache(String username) {
        int attempts = 0;
        try {
            attempts = bruteForceAttackConfigurationProperties.attemptIncrement() + loginAttemptCache.get(username);
        } catch (Exception e) {
            log.error("Username not found in cache", e);
        }
        loginAttemptCache.put(username, attempts);
    }

    @Override
    public boolean hasExceedsMaxAttempts(String username) {
        try {
            return loginAttemptCache.get(username) >= bruteForceAttackConfigurationProperties.maximumNumberOfAttempts();
        } catch (Exception e) {
            log.error("Username not found in cache", e);
        }
        return false;
    }
}
