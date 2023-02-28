package com.kanwise.kanwise_service.service.security;

public interface IMemberAuthenticationFacade {
    boolean isAdmin();

    boolean isMemberByUsername(String username);

    boolean isMemberByUsernameAndHasAuthority(String username, String authority);
}
