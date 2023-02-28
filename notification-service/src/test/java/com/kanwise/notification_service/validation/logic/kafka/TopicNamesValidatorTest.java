package com.kanwise.notification_service.validation.logic.kafka;

import com.kanwise.notification_service.model.kafka.TopicType;
import com.kanwise.notification_service.validation.annotation.kafka.TopicNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.stream.Stream;

import static com.kanwise.notification_service.model.kafka.TopicType.NOTIFICATION_EMAIL;
import static com.kanwise.notification_service.model.kafka.TopicType.NOTIFICATION_SMS;
import static java.util.Map.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicNamesValidatorTest {

    private TopicNamesValidator validator;

    @Mock
    private TopicNames constraintAnnotation;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    static Stream<Map<TopicType, String>> validTopicNamesMaps() {
        return Stream.of(
                of(
                        NOTIFICATION_SMS, "notification-sms",
                        NOTIFICATION_EMAIL, "notification-email"),
                of(
                        NOTIFICATION_SMS, "notification-sms-2",
                        NOTIFICATION_EMAIL, "notification-email-2"),
                of(
                        NOTIFICATION_SMS, "notification_sms_3",
                        NOTIFICATION_EMAIL, "notification-email-3"),
                of(
                        NOTIFICATION_SMS, "notification_sms_4",
                        NOTIFICATION_EMAIL, "notification_email_4"),
                of(
                        NOTIFICATION_SMS, "5",
                        NOTIFICATION_EMAIL, "notification"));
    }

    static Stream<Map<TopicType, String>> invalidTopicNamesMaps() {
        return Stream.of(
                of(
                        NOTIFICATION_SMS, "notification-sms*",
                        NOTIFICATION_EMAIL, "not!fication-email"),
                of(
                        NOTIFICATION_SMS, "%",
                        NOTIFICATION_EMAIL, "notification-email-2"),
                of(
                        NOTIFICATION_SMS, "$$###",
                        NOTIFICATION_EMAIL, "#$%&"),
                of(
                        NOTIFICATION_SMS, "notification_sms_4.",
                        NOTIFICATION_EMAIL, "notification_email_4"),
                of(
                        NOTIFICATION_SMS, "*",
                        NOTIFICATION_EMAIL, "notification"));
    }

    @BeforeEach
    void setUp() {
        validator = new TopicNamesValidator();
        validator.initialize(constraintAnnotation);
    }

    @MethodSource("validTopicNamesMaps")
    @ParameterizedTest
    void shouldReturnTrueIfTopicNamesAreValid(Map<TopicType, String> validTopicNames) {
        // Given
        // When
        // Then
        assertTrue(validator.isValid(validTopicNames, constraintValidatorContext));
    }

    @MethodSource("invalidTopicNamesMaps")
    @ParameterizedTest
    void shouldReturnFalseIfTopicNamesAreInvalid(Map<TopicType, String> invalidTopicNames) {
        // Given
        // When
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));
        // Then
        assertFalse(validator.isValid(invalidTopicNames, constraintValidatorContext));
    }

    @Test
    void shouldReturnFalseIfTopicNamesMapIsNull() {
        // Given
        // When
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));
        // Then
        assertFalse(validator.isValid(null, constraintValidatorContext));
    }
}