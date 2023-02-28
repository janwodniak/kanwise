package com.kanwise.report_service.model.http;

public @interface HttpHeader {
    String USERNAME = "username";
    String ROLE = "role";

    String value();
}