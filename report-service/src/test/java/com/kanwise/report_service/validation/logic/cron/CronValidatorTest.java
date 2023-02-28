package com.kanwise.report_service.validation.logic.cron;

import com.kanwise.report_service.validation.annotation.cron.Cron;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CronValidatorTest {

    private CronValidator validator;

    @Mock
    private Cron constraintAnnotation;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    void setUp() {
        validator = new CronValidator();
        validator.initialize(constraintAnnotation);
    }

    @Test
    void shouldReturnTrueIfCronExpressionIsValid() {
        // Given
        String validCronExpression = "0 0 12 * * ? *";
        // When
        boolean isValid = validator.isValid(validCronExpression, constraintValidatorContext);
        // Then
        assertTrue(isValid);
    }

    @Test
    void shouldReturnTrueIfCronExpressionIsNull() {
        // Given
        // When
        boolean isValid = validator.isValid(null, constraintValidatorContext);
        // Then
        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseIfCronExpressionIsInvalid() {
        // Given
        String invalidCronExpression = "invalid-cron-expression";
        // When
        boolean isValid = validator.isValid(invalidCronExpression, constraintValidatorContext);
        // Then
        assertFalse(isValid);
    }
}