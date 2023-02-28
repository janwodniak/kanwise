package com.kanwise.user_service.validation.logic.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class NullOrNotBlankValidatorTest {

    private NullOrNotBlankValidator validator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    static Stream<String> blankStringsValues() {
        return of("", " ", "  ", "   ", "\t", "\n", "\r");
    }

    static Stream<String> notBlankStringsValues() {
        return of("a", " a", "a ", " a ", "a\t", "a \t", "a\t ", "a \t");
    }

    @BeforeEach
    void setUp() {
        validator = new NullOrNotBlankValidator();
    }

    @Test
    void shouldReturnTrueWhenValueIsNull() {
        // Given
        // When
        // Then
        assertTrue(validator.isValid(null, constraintValidatorContext));
    }

    @MethodSource("blankStringsValues")
    @ParameterizedTest
    void shouldReturnFalseWhenValueIsBlank(String testValue) {
        // Given
        // When
        // Then
        assertFalse(validator.isValid(testValue, constraintValidatorContext));
    }

    @MethodSource("notBlankStringsValues")
    @ParameterizedTest
    void shouldReturnTrueWhenValueIsNotBlank(String testValue) {
        // Given
        // When
        // Then
        assertTrue(validator.isValid(testValue, constraintValidatorContext));
    }
}