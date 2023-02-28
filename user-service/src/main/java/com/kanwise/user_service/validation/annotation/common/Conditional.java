package com.kanwise.user_service.validation.annotation.common;

import com.kanwise.user_service.validation.logic.common.ConditionalValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(Conditionals.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ConditionalValidator.class})
public @interface Conditional {

    String message() default "REQUIRED_FIELD";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String selected();

    String[] required();

    String[] values();
}

