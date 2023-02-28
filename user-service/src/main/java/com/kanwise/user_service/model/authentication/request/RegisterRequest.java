package com.kanwise.user_service.model.authentication.request;

import com.kanwise.user_service.validation.annotation.common.Conditional;
import com.kanwise.user_service.validation.annotation.email.EmailPattern;
import com.kanwise.user_service.validation.annotation.email.UniqueEmail;
import com.kanwise.user_service.validation.annotation.phone_number.PhoneNumberPattern;
import com.kanwise.user_service.validation.annotation.username.UniqueUsername;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Conditional(selected = "twoFactorEnabled", values = {"true"}, required = {"phoneNumber"}, message = "PHONE_NUMBER_REQUIRED_FOR_TWO_FACTOR_AUTH")
@Builder
public record RegisterRequest(
        @NotNull(message = "FIRST_NAME_NOT_NULL") @NotBlank(message = "FIRST_NAME_NOT_BLANK") String firstName,
        @NotNull(message = "LAST_NAME_NOT_NULL") @NotBlank(message = "LAST_NAME_NOT_BLANK") String lastName,
        @NotNull(message = "USERNAME_NOT_NULL") @NotBlank(message = "USERNAME_NOT_BLANK") @UniqueUsername String username,
        @NotNull(message = "EMAIL_NOT_NULL") @UniqueEmail @EmailPattern String email,
        @PhoneNumberPattern String phoneNumber,
        boolean twoFactorEnabled
) {

}
