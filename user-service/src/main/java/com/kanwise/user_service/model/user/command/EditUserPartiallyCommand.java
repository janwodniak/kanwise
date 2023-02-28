package com.kanwise.user_service.model.user.command;

import com.kanwise.user_service.model.authentication.two_factor_authentication.TwoFactorAction;
import com.kanwise.user_service.model.notification.subscribtions.UserNotificationType;
import com.kanwise.user_service.validation.annotation.common.NullOrNotBlank;
import com.kanwise.user_service.validation.annotation.email.EmailPattern;
import com.kanwise.user_service.validation.annotation.phone_number.PhoneNumberPattern;
import lombok.Builder;

import java.util.Map;


@Builder
public record EditUserPartiallyCommand(
        @NullOrNotBlank(message = "FIRST_NAME_NULL_OR_NOT_BLANK") String firstName,
        @NullOrNotBlank(message = "LAST_NAME_NULL_OR_NOT_BLANK") String lastName,
        @NullOrNotBlank(message = "USERNAME_NULL_OR_NOT_BLANK") String username,
        @NullOrNotBlank(message = "EMAIL_NULL_OR_NOT_BLANK") @EmailPattern String email,

        Map<UserNotificationType, Boolean> notificationSubscriptions,
        Map<TwoFactorAction, Boolean> twoFactorSubscriptions,

        Boolean twoFactorEnabled,
        @PhoneNumberPattern String phoneNumber
) {
}
