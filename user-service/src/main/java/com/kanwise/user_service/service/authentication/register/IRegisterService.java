package com.kanwise.user_service.service.authentication.register;

import com.kanwise.user_service.model.authentication.response.RegisterResponse;
import com.kanwise.user_service.model.user.User;

public interface IRegisterService {
    RegisterResponse register(User user);
}
