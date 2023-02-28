package com.kanwise.user_service.model.authentication.mapping;

import com.kanwise.user_service.model.authentication.request.RegisterRequest;
import com.kanwise.user_service.model.authentication.two_factor_authentication.TwoFactorAction;
import com.kanwise.user_service.model.notification.subscribtions.UserNotificationType;
import com.kanwise.user_service.model.user.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.EnumMap;

import static com.kanwise.user_service.model.security.UserRole.USER;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Service
public class RegisterRequestToUserConverter implements Converter<RegisterRequest, User> {

    private final Clock clock;

    @Override
    public User convert(MappingContext<RegisterRequest, User> mappingContext) {
        RegisterRequest request = mappingContext.getSource();
        return User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(request.username())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .joinDate(now(clock))
                .lastLoginDate(now(clock))
                .twoFactorEnabled(request.twoFactorEnabled())
                .userRole(USER)
                .active(TRUE)
                .isCredentialsNonExpired(TRUE)
                .isAccountNonLocked(TRUE)
                .isAccountNonExpired(TRUE)
                .notificationSubscriptions(generateDefaultNotificationSubscriptions())
                .twoFactorSubscriptions(generateTwoFactorSubscriptions(request.twoFactorEnabled()))
                .isEnabled(!request.twoFactorEnabled())
                .build();
    }

    private EnumMap<TwoFactorAction, Boolean> generateTwoFactorSubscriptions(boolean twoFactorEnabled) {
        EnumMap<TwoFactorAction, Boolean> subscriptions = new EnumMap<>(TwoFactorAction.class);
        for (TwoFactorAction action : TwoFactorAction.values()) {
            subscriptions.put(action, twoFactorEnabled);
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
