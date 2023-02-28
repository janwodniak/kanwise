package com.kanwise.user_service.model.authentication.password;


import com.kanwise.user_service.validation.annotation.common.FieldsValueMatch;
import com.kanwise.user_service.validation.annotation.password.ValidPassword;
import lombok.Builder;

import javax.validation.constraints.NotBlank;


@FieldsValueMatch.List({
        @FieldsValueMatch(
                field = "password",
                fieldMatch = "passwordConfirmation",
                message = "PASSWORDS_DO_NOT_MATCH"
        )
})
@Builder
public record ForgottenPasswordResetCommand(
        @NotBlank(message = "NEW_PASSWORD_NOT_BLANK") @ValidPassword String password,
        @NotBlank(message = "PASSWORD_CONFIRMATION_NOT_BLANK") String passwordConfirmation,
        @NotBlank(message = "TOKEN_NOT_BLANK") String token
) {
}
