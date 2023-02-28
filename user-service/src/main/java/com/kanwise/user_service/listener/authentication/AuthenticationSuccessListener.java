package com.kanwise.user_service.listener.authentication;

import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.service.authentication.login.ILoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener {

    private final ILoginAttemptService loginAttemptService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }
}
