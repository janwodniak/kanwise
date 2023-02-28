package com.kanwise.report_service.model.http;

import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;

public record EndpointSpecification(
        String path,
        HttpMethod method
) {
    public boolean matches(HttpServletRequest request) {
        return new AntPathMatcher().match(path, request.getRequestURI()) && method.matches(request.getMethod());
    }
}
