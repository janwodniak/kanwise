package com.kanwise.user_service.validation.logic.email;

import com.kanwise.user_service.validation.annotation.email.EmailPattern;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;

@Service
@Scope("prototype")
public class EmailPatternValidator implements ConstraintValidator<EmailPattern, String> {

    private String pattern;

    @Override
    public void initialize(EmailPattern constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return ofNullable(email)
                .map(e -> compile(pattern).matcher(e).matches())
                .orElse(true);
    }
}
