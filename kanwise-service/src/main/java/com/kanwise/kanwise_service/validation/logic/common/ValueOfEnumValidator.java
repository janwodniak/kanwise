package com.kanwise.kanwise_service.validation.logic.common;


import com.kanwise.kanwise_service.validation.annotation.common.ValueOfEnum;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Service
@Scope("prototype")
public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, String> {

    private List<String> acceptedValues;

    @Override
    public void initialize(ValueOfEnum annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .toList();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return ofNullable(value)
                .map(v -> acceptedValues.contains(v.toUpperCase()))
                .orElse(true);
    }
}
