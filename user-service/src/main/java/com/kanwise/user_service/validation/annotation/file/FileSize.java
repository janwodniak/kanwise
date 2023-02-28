package com.kanwise.user_service.validation.annotation.file;

import com.kanwise.user_service.validation.logic.file.FileSizeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileSizeValidator.class)
public @interface FileSize {
    String message() default "INVALID_FILE_SIZE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long maxSizeInMB() default 2048L;
}
