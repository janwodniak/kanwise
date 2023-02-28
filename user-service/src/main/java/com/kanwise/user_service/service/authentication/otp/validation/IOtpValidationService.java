package com.kanwise.user_service.service.authentication.otp.validation;


import com.kanwise.user_service.model.otp.OtpValidationRequest;

public interface IOtpValidationService<T extends OtpValidationRequest> {

    void validateOtp(T otpValidationRequest);

}
