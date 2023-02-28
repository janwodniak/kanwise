package com.kanwise.user_service.validation.annotation.username;


import com.kanwise.user_service.validation.logic.username.UniqueUsernameValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUsernameValidator.class)
public @interface UniqueUsername {
    String message() default "USERNAME_NOT_UNIQUE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
