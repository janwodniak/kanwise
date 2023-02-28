package com.kanwise.user_service.service.authentication.login.implementation;

import com.kanwise.user_service.jwt.JwtTokenProvider;
import com.kanwise.user_service.model.authentication.request.LoginRequest;
import com.kanwise.user_service.model.authentication.response.LoginResponse;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.model.user.UserPrincipal;
import com.kanwise.user_service.service.authentication.login.ILoginAttemptService;
import com.kanwise.user_service.service.authentication.login.ILoginService;
import com.kanwise.user_service.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.kanwise.user_service.constant.SecurityConstant.BEARER_PREFIX;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
@Service
public class LoginService implements ILoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ILoginAttemptService loginAttemptService;
    private final IUserService userService;
    private final Clock clock;

    @Transactional
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userService.findByUsername(loginRequest.username());
        processLoginAttempt(user);
        authenticate(loginRequest.username(), loginRequest.password());
        return LoginResponse.builder()
                .user(user)
                .jwtHeader(getJwtHeader(user))
                .build();
    }

    private void processLoginAttempt(User user) {
        validateLoginAttempt(user);
        user.setLastLoginDate(LocalDateTime.now(clock));
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private HttpHeaders getJwtHeader(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, BEARER_PREFIX + jwtTokenProvider.generateToken(new UserPrincipal(user)));
        return headers;
    }

    private void validateLoginAttempt(User user) {
        if (user.isAccountNonLocked()) {
            user.setAccountNonLocked(!loginAttemptService.hasExceedsMaxAttempts(user.getUsername()));
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }
}
