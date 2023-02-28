package com.kanwise.user_service.model.authentication.mapping;

import com.kanwise.user_service.model.authentication.response.RegisterResponse;
import com.kanwise.user_service.model.authentication.response.dto.RegisterResponseDto;
import com.kanwise.user_service.model.otp.OneTimePassword;
import com.kanwise.user_service.model.user.User;
import com.kanwise.user_service.model.user.dto.UserDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;


@Service
public class RegisterResponseToRegisterResponseDtoConverter implements Converter<RegisterResponse, RegisterResponseDto> {
    @Override
    public RegisterResponseDto convert(MappingContext<RegisterResponse, RegisterResponseDto> mappingContext) {
        RegisterResponse registerResponse = mappingContext.getSource();
        return RegisterResponseDto.builder()
                .user(convertUserToUserDto(registerResponse.user()))
                .otpId(registerResponse.oneTimePassword().map(OneTimePassword::getId))
                .build();
    }

    private UserDto convertUserToUserDto(User user) {
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
                .build();
    }
}
