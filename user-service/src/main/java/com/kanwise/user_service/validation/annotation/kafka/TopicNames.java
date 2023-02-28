package com.kanwise.user_service.validation.annotation.kafka;

import com.kanwise.user_service.validation.logic.kafka.TopicNamesValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TopicNamesValidator.class)
public @interface TopicNames {
    String message() default "MISSING_TOPIC_NAME";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
