package com.kanwise.user_service.validation.logic.phone_number;

import com.kanwise.user_service.validation.annotation.phone_number.PhoneNumberPattern;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;

@Service
@Scope("prototype")
public class PhoneNumberPatternValidator implements ConstraintValidator<PhoneNumberPattern, String> {

    private String pattern;

    @Override
    public void initialize(PhoneNumberPattern constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext constraintValidatorContext) {
        return ofNullable(phoneNumber)
                .map(p -> compile(pattern).matcher(p).matches())
                .orElse(true);
    }
}
