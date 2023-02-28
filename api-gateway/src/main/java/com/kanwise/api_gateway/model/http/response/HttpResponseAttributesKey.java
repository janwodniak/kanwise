package com.kanwise.api_gateway.model.http.response;

import lombok.Getter;

@Getter
public enum HttpResponseAttributesKey {
    TIMESTAMP("timestamp"),
    HTTP_STATUS_CODE("httpStatusCode"),
    HTTP_STATUS("httpStatus"),
    REASON("reason"),
    MESSAGE("message");

    private final String key;

    HttpResponseAttributesKey(String key) {
        this.key = key;
    }
}
