package com.kanwise.user_service.model.authentication.response;

import com.kanwise.user_service.model.otp.OneTimePassword;
import com.kanwise.user_service.model.user.User;
import lombok.Builder;

import java.util.Optional;

@Builder
public record RegisterResponse(User user, Optional<OneTimePassword> oneTimePassword) {
}
