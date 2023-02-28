package com.kanwise.report_service.validation.logic.cron;

import com.kanwise.report_service.validation.annotation.cron.Cron;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Optional.ofNullable;

@Service
public class CronValidator implements ConstraintValidator<Cron, String> {

    @Override
    public boolean isValid(String cron, ConstraintValidatorContext constraintValidatorContext) {
        return ofNullable(cron)
                .map(CronExpression::isValidExpression)
                .orElse(true);
    }
}
