package com.kanwise.user_service.validation.logic.password;


import com.kanwise.user_service.error.handling.MessageFormatter;
import com.kanwise.user_service.validation.annotation.password.ValidPassword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String>, MessageFormatter {

    private final PasswordValidator passwordValidator;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return Optional.ofNullable(password).map(p -> {

                    RuleResult result = passwordValidator.validate(new PasswordData(password));
                    if (result.isValid()) {
                        return true;
                    }
                    context.disableDefaultConstraintViolation();
                    passwordValidator.getMessages(result)
                            .forEach(message -> context.buildConstraintViolationWithTemplate(formatMessage(message, true)).addConstraintViolation());
                    return false;
                })
                .orElseGet(() -> {
                            context.disableDefaultConstraintViolation();
                            context.buildConstraintViolationWithTemplate("PASSWORD_NOT_NULL").addConstraintViolation();
                            return false;
                        }
                );
    }
}
