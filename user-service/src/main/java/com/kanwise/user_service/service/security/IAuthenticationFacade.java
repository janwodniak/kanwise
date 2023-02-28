package com.kanwise.user_service.service.security;

public interface IAuthenticationFacade {

    boolean isAdmin();

    boolean isUserById(long id);

    boolean isUserByIdAndHasAuthority(long id, String authority);

}
