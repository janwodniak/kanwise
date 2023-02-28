package com.kanwise.user_service.validation.logic.common;

import com.kanwise.user_service.validation.annotation.common.FieldsValueMatch;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FieldsValueMatchValidatorTest {

    private FieldsValueMatchValidator fieldsValueMatchValidator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private FieldsValueMatch constraintAnnotation;

    @BeforeEach
    void setUp() {
        fieldsValueMatchValidator = new FieldsValueMatchValidator();
        when(constraintAnnotation.field()).thenReturn("field");
        when(constraintAnnotation.fieldMatch()).thenReturn("fieldMatch");
        when(constraintAnnotation.message()).thenReturn("message");
        fieldsValueMatchValidator.initialize(constraintAnnotation);
    }


    @Test
    void shouldReturnTrueWhenValuesMatch() {
        // Given
        String testValue = "testValue";
        TestClass testClass = TestClass.builder()
                .field(testValue)
                .fieldMatch(testValue)
                .build();
        // When
        // Then
        Assertions.assertTrue(fieldsValueMatchValidator.isValid(testClass, constraintValidatorContext));
        Assertions.assertEquals(testClass.getField(), testClass.getFieldMatch());
    }

    @Test
    void shouldReturnTrueWhenBothValuesAreNull() {
        // Given
        TestClass testClass = TestClass
                .builder()
                .field(null)
                .fieldMatch(null)
                .build();
        // When
        // Then
        Assertions.assertTrue(fieldsValueMatchValidator.isValid(testClass, constraintValidatorContext));
        Assertions.assertEquals(testClass.getField(), testClass.getFieldMatch());
    }

    @Data
    @Builder
    public static class TestClass {
        private String field;
        private String fieldMatch;
    }
}