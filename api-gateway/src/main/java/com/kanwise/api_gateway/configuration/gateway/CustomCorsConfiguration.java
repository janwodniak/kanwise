package com.kanwise.api_gateway.configuration.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import static com.google.common.net.HttpHeaders.X_REQUESTED_WITH;
import static com.kanwise.api_gateway.model.http.method.HttpMethod.DELETE;
import static com.kanwise.api_gateway.model.http.method.HttpMethod.GET;
import static com.kanwise.api_gateway.model.http.method.HttpMethod.PATCH;
import static com.kanwise.api_gateway.model.http.method.HttpMethod.POST;
import static com.kanwise.api_gateway.model.http.method.HttpMethod.PUT;
import static java.time.Duration.ofHours;
import static java.util.Arrays.asList;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.ORIGIN;

@RequiredArgsConstructor
@Configuration
public class CustomCorsConfiguration extends CorsConfiguration {

    private final CorsConfigurationProperties corsConfigurationProperties;

    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", getCorsConfiguration());
        return new CorsWebFilter(source);
    }

    private CorsConfiguration getCorsConfiguration() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(corsConfigurationProperties.allowedOrigins());
        corsConfig.setMaxAge(ofHours(1).getSeconds());
        corsConfig.setAllowedHeaders(asList(ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION, ORIGIN, ACCEPT, X_REQUESTED_WITH, ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_HEADERS));
        corsConfig.setExposedHeaders(asList(ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION, ACCESS_CONTROL_ALLOW_ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN, ACCESS_CONTROL_ALLOW_CREDENTIALS));
        corsConfig.setAllowedMethods(asList(GET, POST, DELETE, PUT, PATCH));
        return corsConfig;
    }
}