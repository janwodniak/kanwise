package com.kanwise.report_service.validation.annotation.cron;


import com.kanwise.report_service.validation.logic.cron.CronValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CronValidator.class)
public @interface Cron {
    String message() default "INVALID_CRON_PATTERN";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
