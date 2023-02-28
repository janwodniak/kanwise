package com.kanwise.clients.user_service.authentication.model;

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
public class OtpSmsResponse extends SmsResponse {
    private OtpStatus status;
    private long otpId;
}
