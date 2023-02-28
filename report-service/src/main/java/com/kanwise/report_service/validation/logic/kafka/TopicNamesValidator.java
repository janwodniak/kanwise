package com.kanwise.report_service.validation.logic.kafka;

import com.kanwise.report_service.model.kafka.TopicType;
import com.kanwise.report_service.validation.annotation.kafka.TopicNames;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Optional;

@Service
public class TopicNamesValidator implements ConstraintValidator<TopicNames, Map<TopicType, String>> {

    private static void setConstraintValidationContextMessage(ConstraintValidatorContext constraintValidatorContext, String message) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    @Override
    public boolean isValid(Map<TopicType, String> topicTypeStringMap, ConstraintValidatorContext constraintValidatorContext) {
        return Optional.ofNullable(topicTypeStringMap)
                .map(map -> map.values().stream().allMatch(topicName -> {
                    if (topicName.matches("^[a-zA-Z0-9_-]+$")) {
                        return true;
                    } else {
                        setConstraintValidationContextMessage(constraintValidatorContext, "TOPIC_NAME_CAN_ONLY_CONTAIN_LETTERS_NUMBERS_UNDERSCORES_AND_DASHES");
                        return false;
                    }
                })).orElseGet(() -> {
                    setConstraintValidationContextMessage(constraintValidatorContext, "TOPIC_NAMES_MAP_NOT_NULL");
                    return false;
                });
    }
}
