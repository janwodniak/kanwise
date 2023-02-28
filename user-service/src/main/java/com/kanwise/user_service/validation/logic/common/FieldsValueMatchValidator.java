package com.kanwise.user_service.validation.logic.common;


import com.kanwise.user_service.validation.annotation.common.FieldsValueMatch;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Service
@Scope("prototype")
public class FieldsValueMatchValidator implements ConstraintValidator<FieldsValueMatch, Object> {

    private String field;
    private String fieldMatch;
    private String message;

    @Override
    public void initialize(FieldsValueMatch constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.fieldMatch = constraintAnnotation.fieldMatch();
        this.message = constraintAnnotation.message();
    }

    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object fieldValue = new BeanWrapperImpl(value)
                .getPropertyValue(field);
        Object fieldMatchValue = new BeanWrapperImpl(value)
                .getPropertyValue(fieldMatch);

        if (fieldValue != null) {
            boolean valid = fieldValue.equals(fieldMatchValue);
            if (!valid) {
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(fieldMatch)
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
            }
            return valid;
        } else {
            return fieldMatchValue == null;
        }
    }
}