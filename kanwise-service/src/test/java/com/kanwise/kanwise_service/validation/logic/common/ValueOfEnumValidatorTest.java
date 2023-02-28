package com.kanwise.kanwise_service.validation.logic.common;

import com.kanwise.kanwise_service.validation.annotation.common.ValueOfEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ValueOfEnumValidatorTest {

    private ValueOfEnumValidator validator;

    @Mock
    private ValueOfEnum constraintAnnotation;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    static Stream<String> presentValuesOfTestEnum() {
        return Stream.of(TestEnum.values()).map(TestEnum::name);
    }

    static Stream<String> absentValuesOfTestEnum() {
        return Stream.of("TEST4", "TEST5", "TEST6");
    }

    @BeforeEach
    void setUp() {
        validator = new ValueOfEnumValidator();
    }

    @MethodSource("presentValuesOfTestEnum")
    @ParameterizedTest
    void shouldReturnTrueIfStringValueOfEnumIsPresent(String testValue) {
        // Given
        Class<?> testEnum = TestEnum.class;
        // When
        Mockito.<Class<?>>when(constraintAnnotation.enumClass()).thenReturn(testEnum);
        validator.initialize(constraintAnnotation);
        // Then
        assertTrue(validator.isValid(testValue, constraintValidatorContext));
    }

    @MethodSource("absentValuesOfTestEnum")
    @ParameterizedTest
    void shouldReturnFalseIfStringValueOfEnumIsAbsent(String testValue) {
        // Given
        Class<?> testEnum = TestEnum.class;
        // When
        Mockito.<Class<?>>when(constraintAnnotation.enumClass()).thenReturn(testEnum);
        validator.initialize(constraintAnnotation);
        // Then
        assertFalse(validator.isValid(testValue, constraintValidatorContext));
    }

    enum TestEnum {
        TEST1, TEST2, TEST3
    }
}