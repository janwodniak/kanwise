package com.kanwise.api_gateway.model.http.response;

import lombok.experimental.UtilityClass;


@UtilityClass
public class ErrorMessage {
    public static final String CONNECTION_ERROR_TRY_AGAIN_LATER = "CONNECTION_ERROR_TRY_AGAIN_LATER";
    public static final String AUTHENTICATION_HEADER_IS_NOT_PRESENT = "AUTHENTICATION_HEADER_IS_NOT_PRESENT";
    public static final String TOKEN_IS_NOT_VALID = "TOKEN_IS_NOT_VALID";
}
