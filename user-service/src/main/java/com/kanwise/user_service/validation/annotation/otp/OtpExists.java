package com.kanwise.user_service.validation.annotation.otp;

import com.kanwise.user_service.validation.logic.otp.OtpExistsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OtpExistsValidator.class)
public @interface OtpExists {
    String message() default "OTP_NOT_FOUND";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
