package com.kanwise.user_service.validation.logic.common;

import com.kanwise.user_service.validation.annotation.common.Conditional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@Scope("prototype")
public class ConditionalValidator implements ConstraintValidator<Conditional, Object> {

    private String selected;
    private String[] required;
    private String message;
    private String[] values;

    private static boolean isValid(Object requiredValue) {
        return requiredValue != null && !isEmpty(requiredValue);
    }

    private static Object getRequiredValue(Object objectToValidate, String propName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object requiredValue;
        if (objectToValidate instanceof Record recordToValidate) {
            requiredValue = getRequiredObjectFromRecord(recordToValidate, propName);
        } else {
            requiredValue = BeanUtils.getProperty(objectToValidate, propName);
        }
        return requiredValue;
    }

    private static Object getRequiredObjectFromRecord(Record objectToValidate, String requiredPropertyName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        RecordComponent[] recordComponents = getRecordComponents(objectToValidate);
        String[] fieldNames = getFieldNamesFromRecordComponents(recordComponents);
        int selectedIndex = asList(fieldNames).indexOf(requiredPropertyName);
        if (selectedIndex == -1) {
            throw new NoSuchMethodException("Field %s is not present in record".formatted(requiredPropertyName));
        }
        return recordComponents[selectedIndex].getAccessor().invoke(objectToValidate);
    }

    private static String[] getFieldNamesFromRecordComponents(RecordComponent[] recordComponents) {
        return stream(recordComponents)
                .map(RecordComponent::getName)
                .toArray(String[]::new);
    }

    private static RecordComponent[] getRecordComponents(Record objectToValidate) {
        return objectToValidate.getClass().getRecordComponents();
    }

    @Override
    public void initialize(Conditional requiredIfChecked) {
        selected = requiredIfChecked.selected();
        required = requiredIfChecked.required();
        message = requiredIfChecked.message();
        values = requiredIfChecked.values();
    }

    @Override
    public boolean isValid(Object objectToValidate, ConstraintValidatorContext context) {
        boolean valid = true;
        try {
            if (existsInValues(getActualValue(objectToValidate))) {
                valid = validateRequiredValues(objectToValidate, context);
            }
        } catch (IllegalAccessException e) {
            log.error("ACCESSOR_METHOD_NOT_AVAILABLE_FOR_CLASS_{}_EXCEPTION_{}", objectToValidate.getClass().getName(), e);
            return false;
        } catch (NoSuchMethodException e) {
            log.error("FIELD_OR_METHOD_IS_NOT_PRESENT_ON_CLASS_{}_EXCEPTION_{}", objectToValidate.getClass().getName(), e);
            return false;
        } catch (InvocationTargetException e) {
            log.error("AN_EXCEPTION_OCCURRED_WHEN_ACCESSING_CLASS_{}_EXCEPTION_{}", objectToValidate.getClass().getName(), e);
            return false;
        }
        return valid;
    }

    private boolean validateRequiredValues(Object objectToValidate, ConstraintValidatorContext context) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        boolean valid = true;
        for (String requiredPropertyName : required) {
            Object requiredValue = getRequiredValue(objectToValidate, requiredPropertyName);
            valid = isValid(requiredValue);
            if (!valid) {
                buildConstrainViolation(context, requiredPropertyName);
            }
        }
        return valid;
    }

    private void buildConstrainViolation(ConstraintValidatorContext context, String requiredPropertyName) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(requiredPropertyName).addConstraintViolation();
    }

    private boolean existsInValues(String actualValue) {
        return asList(values).contains(actualValue);
    }

    private String getActualValue(Object objectToValidate) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (objectToValidate instanceof Record recordToValidate) {
            return getActualValueFromRecord(recordToValidate);
        } else {
            return BeanUtils.getProperty(objectToValidate, selected);
        }
    }

    private String getActualValueFromRecord(Record objectToValidate) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        RecordComponent[] recordComponents = getRecordComponents(objectToValidate);
        String[] fieldNames = getFieldNamesFromRecordComponents(recordComponents);
        int selectedIndex = asList(fieldNames).indexOf(selected);
        if (selectedIndex == -1) {
            throw new NoSuchMethodException("FIELD_%s_IS_NOT_PRESENT_IN_RECORD".formatted(selected));
        }
        return recordComponents[selectedIndex].getAccessor().invoke(objectToValidate).toString();
    }

}

