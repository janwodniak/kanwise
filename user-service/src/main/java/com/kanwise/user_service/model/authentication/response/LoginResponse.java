package com.kanwise.user_service.model.authentication.response;

import com.kanwise.user_service.model.user.User;
import lombok.Builder;
import org.springframework.http.HttpHeaders;

@Builder
public record LoginResponse(User user, HttpHeaders jwtHeader) {
}
