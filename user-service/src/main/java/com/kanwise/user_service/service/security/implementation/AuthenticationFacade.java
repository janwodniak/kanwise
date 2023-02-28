package com.kanwise.user_service.service.security.implementation;

import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.service.security.IAuthenticationFacade;
import com.kanwise.user_service.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.kanwise.user_service.constant.SecurityConstant.ROLE_ADMIN;

@Component
@RequiredArgsConstructor
public class AuthenticationFacade implements IAuthenticationFacade {

    private final IUserService userService;

    @Override
    public boolean isAdmin() {
        return getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN));
    }

    @Override
    public boolean isUserById(long id) {
        return getUser().map(user -> user.getId() == id).orElse(false);
    }

    @Override
    public boolean isUserByIdAndHasAuthority(long id, String authority) {
        return isUserById(id) && getAuthentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority));
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Optional<User> getUser() {
        return userService.findUserByUsername(getUsername());
    }
}
