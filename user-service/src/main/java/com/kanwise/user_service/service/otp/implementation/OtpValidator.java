package com.kanwise.user_service.service.otp.implementation;

import com.kanwise.user_service.error.custom.security.otp.OtpAlreadyConfirmedException;
import com.kanwise.user_service.error.custom.security.otp.OtpHasExpiredException;
import com.kanwise.user_service.error.custom.security.otp.OtpInvalidCodeException;
import com.kanwise.user_service.error.custom.security.otp.OtpNotDeliveredException;
import com.kanwise.user_service.model.otp.OneTimePassword;
import com.kanwise.user_service.service.otp.IOtpValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;

@RequiredArgsConstructor
@Service
public class OtpValidator implements IOtpValidator {

    private final Clock clock;

    @Override
    public void validateOtp(OneTimePassword oneTimePassword, String code) {
        if (oneTimePassword.isConfirmed()) {
            throw new OtpAlreadyConfirmedException();
        }

        if (!oneTimePassword.isDelivered()) {
            throw new OtpNotDeliveredException();
        }

        if (oneTimePassword.isExpired(clock)) {
            throw new OtpHasExpiredException();
        }

        if (!oneTimePassword.getCode().equals(code)) {
            throw new OtpInvalidCodeException();
        }
    }
}
