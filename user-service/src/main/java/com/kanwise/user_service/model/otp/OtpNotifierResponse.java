package com.kanwise.user_service.model.otp;

import com.kanwise.clients.user_service.authentication.model.OtpStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class OtpNotifierResponse {
    private OtpStatus status;
    private String message;
}
