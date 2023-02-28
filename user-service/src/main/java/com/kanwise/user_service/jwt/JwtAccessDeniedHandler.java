package com.kanwise.user_service.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.user_service.service.response.IHttpResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static com.kanwise.user_service.constant.SecurityConstant.ACCESS_DENIED_MESSAGE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RequiredArgsConstructor
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final IHttpResponseService httpResponseService;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(UNAUTHORIZED.value());
        try (OutputStream outputStream = response.getOutputStream()) {
            objectMapper.writeValue(outputStream, httpResponseService.generateHttpResponse(UNAUTHORIZED, ACCESS_DENIED_MESSAGE));
        }
    }
}
