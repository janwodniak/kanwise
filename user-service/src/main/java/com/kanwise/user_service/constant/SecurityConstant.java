package com.kanwise.user_service.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityConstant {
    public static final String AUTHORITIES = "authorities";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ACCESS_DENIED_MESSAGE = "YOU_ARE_NOT_AUTHORIZED_TO_ACCESS_THIS_RESOURCE";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "TOKEN_CANNOT_BE_VERIFIED";
}
