package com.kanwise.user_service.model.user.mapping;


import com.kanwise.user_service.model.authentication.two_factor_authentication.TwoFactorAction;
import com.kanwise.user_service.model.notification.subscribtions.UserNotificationType;
import com.kanwise.user_service.model.security.UserRole;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.model.user.command.CreateUserCommand;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.EnumMap;
import java.util.Map;

import static java.lang.Boolean.TRUE;
import static java.time.LocalDateTime.now;


@RequiredArgsConstructor
@Service
public class CreateUserCommandToUserConverter implements Converter<CreateUserCommand, User> {

    private final Clock clock;

    @Override
    public User convert(MappingContext<CreateUserCommand, User> mappingContext) {
        CreateUserCommand command = mappingContext.getSource();
        return User.builder()
                .firstName(command.firstName())
                .lastName(command.lastName())
                .username(command.username())
                .email(command.email())
                .joinDate(now(clock))
                .lastLoginDate(now(clock))
                .active(TRUE)
                .userRole(UserRole.USER)
                .isCredentialsNonExpired(TRUE)
                .isAccountNonLocked(TRUE)
                .isAccountNonExpired(TRUE)
                .isEnabled(TRUE)
                .notificationSubscriptions(generateDefaultNotificationSubscriptions())
                .twoFactorSubscriptions(generateDefaultTwoFactorSubscriptions())
                .build();
    }

    private Map<TwoFactorAction, Boolean> generateDefaultTwoFactorSubscriptions() {
        EnumMap<TwoFactorAction, Boolean> subscriptions = new EnumMap<>(TwoFactorAction.class);
        for (TwoFactorAction action : TwoFactorAction.values()) {
            subscriptions.put(action, TRUE);
        }
        return subscriptions;
    }

    private EnumMap<UserNotificationType, Boolean> generateDefaultNotificationSubscriptions() {
        EnumMap<UserNotificationType, Boolean> subscriptions = new EnumMap<>(UserNotificationType.class);
        for (UserNotificationType type : UserNotificationType.values()) {
            subscriptions.put(type, TRUE);
        }
        return subscriptions;
    }
}

