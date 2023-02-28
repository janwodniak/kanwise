package com.kanwise.user_service.validation.annotation.email;


import com.kanwise.user_service.validation.logic.email.EmailExistsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailExistsValidator.class)
public @interface EmailExists {
    String message() default "EMAIL_NOT_FOUND";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
