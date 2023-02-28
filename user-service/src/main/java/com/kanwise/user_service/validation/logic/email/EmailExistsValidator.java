package com.kanwise.user_service.validation.logic.email;

import com.kanwise.user_service.service.user.IUserService;
import com.kanwise.user_service.validation.annotation.email.EmailExists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Service
@RequiredArgsConstructor
public class EmailExistsValidator implements ConstraintValidator<EmailExists, String> {

    private final IUserService userService;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return userService.existsByEmail(email);
    }
}
