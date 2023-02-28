package com.kanwise.user_service.service.authentication;

import com.kanwise.user_service.model.user.User;

public interface IAuthenticationService {

    User validateJwtToken(String tokenParts);
}
