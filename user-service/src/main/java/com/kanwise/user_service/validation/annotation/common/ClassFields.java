package com.kanwise.user_service.validation.annotation.common;


import com.kanwise.user_service.validation.logic.common.ClassFieldsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ClassFieldsValidator.class)
public @interface ClassFields {
    String message() default "INVALID_CLASS_FIELDS_VALUE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<?> fieldsSource();

    String[] excludedFieldsNames() default {};
}
