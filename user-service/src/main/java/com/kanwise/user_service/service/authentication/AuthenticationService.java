package com.kanwise.user_service.service.authentication;

import com.kanwise.user_service.error.custom.user.UserIsDisabledException;
import com.kanwise.user_service.jwt.JwtTokenProvider;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthenticationService implements IAuthenticationService {

    private final IUserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public User validateJwtToken(String token) {
        String username = jwtTokenProvider.getSubject(token);
        return ofNullable(userService.findByUsername(username))
                .filter(User::isEnabled)
                .orElseThrow(UserIsDisabledException::new);
    }
}
