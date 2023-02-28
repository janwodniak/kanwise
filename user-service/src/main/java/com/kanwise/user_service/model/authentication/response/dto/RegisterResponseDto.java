package com.kanwise.user_service.model.authentication.response.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kanwise.user_service.model.user.dto.UserDto;
import lombok.Builder;

import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@Builder
public record RegisterResponseDto(UserDto user, Optional<Long> otpId) {
}
