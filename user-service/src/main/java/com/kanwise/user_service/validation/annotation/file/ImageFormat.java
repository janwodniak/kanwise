package com.kanwise.user_service.validation.annotation.file;

import com.kanwise.user_service.validation.logic.file.ImageFormatValidator;
import org.apache.commons.imaging.ImageFormats;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = ImageFormatValidator.class)
public @interface ImageFormat {
    String message() default "INVALID_IMAGE_FORMAT";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    ImageFormats[] formats();
}
