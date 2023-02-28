package com.kanwise.user_service.model.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kanwise.user_service.model.authentication.two_factor_authentication.TwoFactorAction;
import com.kanwise.user_service.model.notification.subscribtions.UserNotificationType;
import com.kanwise.user_service.model.security.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserDto {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime lastLoginDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime joinDate;
    boolean twoFactorEnabled;
    Map<UserNotificationType, Boolean> notificationSubscriptions;
    Map<TwoFactorAction, Boolean> twoFactorSubscriptions;
    private long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private UserRole userRole;
}
