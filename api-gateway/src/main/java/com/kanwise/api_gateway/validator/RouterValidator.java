package com.kanwise.api_gateway.validator;

import com.kanwise.api_gateway.configuration.security.SecurityConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class RouterValidator {

    private final Predicate<ServerHttpRequest> isSecured;

    @Autowired
    public RouterValidator(SecurityConfigurationProperties securityConfigurationProperties) {
        this.isSecured = request -> securityConfigurationProperties.openApiEndpoints().stream().noneMatch(uri -> containsUri(request, uri));
    }

    private boolean containsUri(ServerHttpRequest request, String uri) {
        return request.getURI().getPath().contains(uri);
    }

    public boolean isRouteSecured(ServerHttpRequest request) {
        return isSecured.test(request);
    }
}
