package com.kanwise.api_gateway.excpetion.custom;

import com.kanwise.api_gateway.model.http.response.HttpResponse;

public class GatewayAuthorizationException extends RuntimeException {
    public GatewayAuthorizationException(HttpResponse httpResponse) {
        super(httpResponse.message());
    }

    public GatewayAuthorizationException(String message) {
        super(message);
    }
}
