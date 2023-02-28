package com.kanwise.user_service.model.otp;

import com.kanwise.user_service.validation.annotation.otp.OtpExists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OtpValidationRequest {
    @OtpExists
    private Long otpId;
    @NotBlank(message = "CODE_NOT_BLANK")
    @Size(min = 6, max = 6, message = "CODE_MUST_BE_6_DIGITS_LONG")
    private String code;
}
