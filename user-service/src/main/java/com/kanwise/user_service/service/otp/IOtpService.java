package com.kanwise.user_service.service.otp;

import com.kanwise.clients.user_service.authentication.model.OtpStatus;
import com.kanwise.user_service.model.otp.OneTimePassword;

public interface IOtpService {

    OneTimePassword getOneTimePasswordById(long id);

    OneTimePassword saveOneTimePassword(OneTimePassword oneTimePassword);

    OneTimePassword generateOneTimePassword();

    void updateOneTimePasswordStatus(OtpStatus status, long id);

    void confirmOtp(OneTimePassword oneTimePassword);

    boolean existsById(long id);
}
