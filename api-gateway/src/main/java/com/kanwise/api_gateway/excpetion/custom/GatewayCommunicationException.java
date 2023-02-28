package com.kanwise.api_gateway.excpetion.custom;

import com.kanwise.api_gateway.model.http.response.HttpResponse;

public class GatewayCommunicationException extends RuntimeException {
    public GatewayCommunicationException(HttpResponse httpResponse) {
        super(httpResponse.message());
    }

    public GatewayCommunicationException(String message) {
        super(message);
    }
}
