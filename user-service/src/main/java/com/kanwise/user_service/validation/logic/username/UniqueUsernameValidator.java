package com.kanwise.user_service.validation.logic.username;

import com.kanwise.user_service.service.user.IUserService;
import com.kanwise.user_service.validation.annotation.username.UniqueUsername;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    private final IUserService userService;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext constraintValidatorContext) {
        return ofNullable(username)
                .map(u -> !userService.existsByUsername(u))
                .orElse(true);
    }
}
