package com.kanwise.user_service.model.authentication.password;

import com.kanwise.user_service.validation.annotation.common.FieldsValueMatch;
import com.kanwise.user_service.validation.annotation.password.ValidPassword;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@FieldsValueMatch.List({
        @FieldsValueMatch(
                field = "newPassword",
                fieldMatch = "confirmNewPassword",
                message = "PASSWORDS_DO_NOT_MATCH"
        )
})
@Builder
public record PasswordResetCommand(
        @NotNull(message = "USER_ID_NOT_NULL") Long userId,
        @NotBlank(message = "CURRENT_PASSWORD_NOT_BLANK") String currentPassword,
        @ValidPassword @NotBlank(message = "NEW_PASSWORD_NOT_BLANK") String newPassword,
        @NotBlank(message = "NEW_PASSWORD_CONFIRMATION_NOT_BLANK") String confirmNewPassword) {
}
