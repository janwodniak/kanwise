package com.kanwise.user_service.service.authentication.login;

import com.kanwise.user_service.model.authentication.request.LoginRequest;
import com.kanwise.user_service.model.authentication.response.LoginResponse;

public interface ILoginService {
    LoginResponse login(LoginRequest loginRequest);
}
