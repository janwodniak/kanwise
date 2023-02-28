package com.kanwise.kanwise_service.validation.annotation.common;


import com.kanwise.kanwise_service.validation.logic.common.NullOrNotBlankValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = NullOrNotBlankValidator.class)
public @interface NullOrNotBlank {
    String message() default "NULL_OR_NOT_BLANK";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
