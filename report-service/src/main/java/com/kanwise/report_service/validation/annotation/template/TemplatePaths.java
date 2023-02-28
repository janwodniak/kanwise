package com.kanwise.report_service.validation.annotation.template;


import com.kanwise.report_service.validation.logic.template.TemplatePathsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TemplatePathsValidator.class)
public @interface TemplatePaths {

    String message() default "MISSING_TEMPLATE_PATH_IN_INDICATED_DIRECTORY";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
