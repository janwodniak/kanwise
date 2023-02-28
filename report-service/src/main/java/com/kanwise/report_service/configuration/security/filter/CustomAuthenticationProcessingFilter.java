package com.kanwise.report_service.configuration.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.report_service.configuration.security.provider.CustomAuthenticationProvider;
import com.kanwise.report_service.error.exception.MissingRoleHeaderException;
import com.kanwise.report_service.error.exception.MissingUsernameHeaderException;
import com.kanwise.report_service.model.security.UserRole;
import com.kanwise.report_service.service.response.common.IHttpResponseService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import static com.kanwise.report_service.model.http.HttpHeader.ROLE;
import static com.kanwise.report_service.model.http.HttpHeader.USERNAME;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.core.context.SecurityContextHolder.clearContext;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@RequiredArgsConstructor
@Component
public class CustomAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final ShouldNotFilterConfigurationProperties shouldNotFilterConfigurationProperties;

    private final IHttpResponseService httpResponseService;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String username = ofNullable(request.getHeader(USERNAME)).orElseThrow(MissingUsernameHeaderException::new);
            UserRole userRole = UserRole.valueOf(ofNullable(request.getHeader(ROLE)).orElseThrow(MissingRoleHeaderException::new));
            handleRequestAuthentication(request, username, userRole);
            filterChain.doFilter(request, response);
        } catch (MissingUsernameHeaderException | MissingRoleHeaderException exception) {
            response.setContentType(APPLICATION_JSON_VALUE);
            response.setStatus(BAD_REQUEST.value());
            try (OutputStream outputStream = response.getOutputStream()) {
                objectMapper.writeValue(outputStream, httpResponseService.generateHttpResponse(BAD_REQUEST, exception.getMessage()));
                outputStream.flush();
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return shouldNotFilterConfigurationProperties.authentication()
                .stream()
                .anyMatch(endpointSpecification -> endpointSpecification.matches(request));
    }

    private void handleRequestAuthentication(HttpServletRequest request, String username, UserRole userRole) {
        if (isAuthenticationValid(username, userRole)) {
            setAuthentication(request, username, userRole.getGrantedAuthorities());
        } else {
            clearContext();
        }
    }

    private void setAuthentication(HttpServletRequest request, String username, Collection<? extends GrantedAuthority> authorities) {
        Authentication authentication = customAuthenticationProvider.getAuthentication(username, authorities, request);
        getContext().setAuthentication(authentication);
    }

    private boolean isAuthenticationValid(String username, UserRole userRole) {
        return username != null && !username.isEmpty() && userRole != null;
    }
}
