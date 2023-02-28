package com.kanwise.api_gateway.model.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserDto(long id,
                      String userId,
                      String firstName,
                      String lastName,
                      String username,
                      String email,
                      String userRole,
                      @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastLoginDate,
                      @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime joinDate) {
}
