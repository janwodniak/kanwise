package com.kanwise.user_service.listener.authentication;

import com.kanwise.user_service.service.authentication.login.ILoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationFailureListener {

    private final ILoginAttemptService loginAttemptService;

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof String username) {
            loginAttemptService.addUserToLoginAttemptCache(username);
        }
    }
}
