package com.kanwise.user_service.model.user.mapping;

import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.model.user.dto.UserDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;

@Service
public class UserToUserDtoConverter implements Converter<User, UserDto> {

    private static UserDto constructUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .userRole(user.getUserRole())
                .joinDate(user.getJoinDate())
                .lastLoginDate(user.getLastLoginDate())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .notificationSubscriptions(user.getNotificationSubscriptions())
                .twoFactorSubscriptions(user.getTwoFactorSubscriptions())
                .build();
    }

    @Override
    public UserDto convert(MappingContext<User, UserDto> mappingContext) {
        User user = mappingContext.getSource();
        return constructUserDto(user);
    }
}
