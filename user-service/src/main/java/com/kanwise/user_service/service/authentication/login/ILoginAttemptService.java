package com.kanwise.user_service.service.authentication.login;

public interface ILoginAttemptService {
    void evictUserFromLoginAttemptCache(String username);

    void addUserToLoginAttemptCache(String username);

    boolean hasExceedsMaxAttempts(String username);
}
