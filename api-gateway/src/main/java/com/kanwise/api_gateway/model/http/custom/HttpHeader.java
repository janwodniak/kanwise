package com.kanwise.api_gateway.model.http.custom;

public @interface HttpHeader {
    String USERNAME = "username";
    String ROLE = "role";

    String value();
}
