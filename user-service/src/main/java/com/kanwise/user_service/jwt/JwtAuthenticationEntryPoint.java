package com.kanwise.user_service.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.user_service.service.response.IHttpResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {

    private final IHttpResponseService httpResponseService;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(FORBIDDEN.value());
        OutputStream outputStream = response.getOutputStream();
        objectMapper.writeValue(outputStream, httpResponseService.generateHttpResponse(FORBIDDEN, exception.getMessage()));
        outputStream.flush();
    }
}
