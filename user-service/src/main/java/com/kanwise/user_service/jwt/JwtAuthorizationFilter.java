package com.kanwise.user_service.jwt;

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
import java.util.Collection;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.core.context.SecurityContextHolder.clearContext;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@RequiredArgsConstructor
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private static boolean isOptionMethodPresent(HttpServletRequest request) {
        return request.getMethod().equalsIgnoreCase(OPTIONS.name());
    }

    private static boolean isAuthenticationAbsent() {
        return getContext().getAuthentication() == null;
    }

    private void setAuthentication(HttpServletRequest request, String username, Collection<? extends GrantedAuthority> authorities) {
        Authentication authentication = jwtTokenProvider.getAuthentication(username, authorities, request);
        getContext().setAuthentication(authentication);
    }

    private boolean isAuthorizationHeaderValid(String authorizationHeader) {
        return authorizationHeader == null || !jwtTokenProvider.startsWithPrefix(authorizationHeader);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isOptionMethodPresent(request)) {
            response.setStatus(OK.value());
        } else {
            String authorizationHeader = request.getHeader(AUTHORIZATION);

            if (isAuthorizationHeaderValid(authorizationHeader)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = jwtTokenProvider.getTokenWithoutPrefix(authorizationHeader);
            String username = jwtTokenProvider.getSubject(token);

            handleRequestAuthentication(request, token, username);
        }
        filterChain.doFilter(request, response);
    }

    private void handleRequestAuthentication(HttpServletRequest request, String token, String username) {
        if (isTokenValidAndAuthenticationIsAbsent(token, username)) {
            setAuthentication(request, username, jwtTokenProvider.getAuthorities(token));
        } else {
            clearContext();
        }
    }

    private boolean isTokenValidAndAuthenticationIsAbsent(String token, String username) {
        return jwtTokenProvider.isTokenValid(username, token) && isAuthenticationAbsent();
    }
}
