package com.kanwise.api_gateway.model.token.custom;

public @interface TokenPrefix {
    String BEARER = "Bearer";

    String value();
}
