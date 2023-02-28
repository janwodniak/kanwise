package com.kanwise.user_service.validation.logic.otp;

import com.kanwise.user_service.service.otp.IOtpService;
import com.kanwise.user_service.validation.annotation.otp.OtpExists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Service
public class OtpExistsValidator implements ConstraintValidator<OtpExists, Long> {

    private final IOtpService otpService;

    @Override
    public boolean isValid(Long otpId, ConstraintValidatorContext constraintValidatorContext) {
        return ofNullable(otpId)
                .map(otpService::existsById)
                .orElse(false);
    }
}
