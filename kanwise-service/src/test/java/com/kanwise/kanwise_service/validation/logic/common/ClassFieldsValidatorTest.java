package com.kanwise.kanwise_service.validation.logic.common;

import com.kanwise.kanwise_service.validation.annotation.common.ClassFields;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassFieldsValidatorTest {

    private ClassFieldsValidator validator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ClassFields constraintAnnotation;

    static Stream<String> presentTestClassFieldsNamesArguments() {
        return of("field1", "field2", "field3");
    }

    static Stream<String> notPresentTestClassFieldsNamesArguments() {
        return of("field4", "field5", "field6");
    }

    @BeforeEach
    void setUp() {
        validator = new ClassFieldsValidator();
    }

    @MethodSource("presentTestClassFieldsNamesArguments")
    @ParameterizedTest
    void shouldReturnTrueIfGivenStringIsPresentAsFieldNameInGivenClass(String testArgument) {
        // Given
        String[] excludedFieldsNames = {};
        Class<?> fieldsSource = TestClass.class;
        // When
        when(constraintAnnotation.excludedFieldsNames()).thenReturn(excludedFieldsNames);
        Mockito.<Class<?>>when(constraintAnnotation.fieldsSource()).thenReturn(fieldsSource);
        validator.initialize(constraintAnnotation);
        // Then
        assertTrue(validator.isValid(testArgument, constraintValidatorContext));
    }

    @MethodSource("presentTestClassFieldsNamesArguments")
    @ParameterizedTest
    void shouldReturnFalseIfGivenStringIsPresentAsFieldNameInGivenClassAndIsExcluded(String testArgument) {
        // Given
        String[] excludedFieldsNames = {testArgument};
        Class<?> fieldsSource = TestClass.class;
        // When
        when(constraintAnnotation.excludedFieldsNames()).thenReturn(excludedFieldsNames);
        Mockito.<Class<?>>when(constraintAnnotation.fieldsSource()).thenReturn(fieldsSource);
        validator.initialize(constraintAnnotation);
        // Then
        assertFalse(validator.isValid(testArgument, constraintValidatorContext));
    }

    @MethodSource("notPresentTestClassFieldsNamesArguments")
    @ParameterizedTest
    void shouldReturnFalseIfGivenStringIsNotPresentAsFieldNameInGivenClass(String testArgument) {
        // Given
        String[] excludedFieldsNames = {};
        Class<?> fieldsSource = TestClass.class;
        // When
        when(constraintAnnotation.excludedFieldsNames()).thenReturn(excludedFieldsNames);
        Mockito.<Class<?>>when(constraintAnnotation.fieldsSource()).thenReturn(fieldsSource);
        validator.initialize(constraintAnnotation);
        // Then
        assertFalse(validator.isValid(testArgument, constraintValidatorContext));
    }

    @MethodSource("notPresentTestClassFieldsNamesArguments")
    @ParameterizedTest
    void shouldReturnFalseIfGivenStringIsNotPresentAsFieldNameAndIsExcluded(String testArgument) {
        // Given
        String[] excludedFieldsNames = {testArgument};
        Class<?> fieldsSource = TestClass.class;
        // When
        when(constraintAnnotation.excludedFieldsNames()).thenReturn(excludedFieldsNames);
        Mockito.<Class<?>>when(constraintAnnotation.fieldsSource()).thenReturn(fieldsSource);
        validator.initialize(constraintAnnotation);
        // Then
        assertFalse(validator.isValid(testArgument, constraintValidatorContext));
    }

    @Data
    public static class TestClass {
        private String field1;
        private int field2;
        private boolean field3;
    }
}