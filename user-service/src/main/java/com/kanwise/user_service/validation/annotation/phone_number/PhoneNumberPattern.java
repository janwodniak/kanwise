package com.kanwise.user_service.validation.annotation.phone_number;

import com.kanwise.user_service.validation.logic.phone_number.PhoneNumberPatternValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberPatternValidator.class)
public @interface PhoneNumberPattern {
    String message() default "INVALID_PHONE_NUMBER_PATTERN";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String pattern() default "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$";
}
