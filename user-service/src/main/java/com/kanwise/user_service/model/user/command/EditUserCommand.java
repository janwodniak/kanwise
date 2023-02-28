package com.kanwise.user_service.model.user.command;

import com.kanwise.user_service.model.authentication.two_factor_authentication.TwoFactorAction;
import com.kanwise.user_service.model.notification.subscribtions.UserNotificationType;
import com.kanwise.user_service.validation.annotation.email.EmailPattern;
import com.kanwise.user_service.validation.annotation.phone_number.PhoneNumberPattern;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Builder
public record EditUserCommand(
        @NotEmpty(message = "FIRST_NAME_NOT_EMPTY") String firstName,
        @NotEmpty(message = "LAST_NAME_NOT_EMPTY") String lastName,
        @NotEmpty(message = "USERNAME_NOT_EMPTY") String username,
        @NotNull(message = "EMAIL_NOT_NULL") @EmailPattern String email,
        @NotNull(message = "NOTIFICATION_SUBSCRIPTIONS_NOT_NULL") Map<UserNotificationType, Boolean> notificationSubscriptions,
        @NotNull(message = "TWO_FACTOR_SUBSCRIPTIONS_NOT_NULL") Map<TwoFactorAction, Boolean> twoFactorSubscriptions,
        @NotNull(message = "TWO_FACTOR_ENABLED_NOT_NULL") Boolean twoFactorEnabled,
        @NotBlank(message = "PHONE_NUMBER_NOT_BLANK") @PhoneNumberPattern String phoneNumber
) {
}
