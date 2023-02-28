package com.kanwise.api_gateway.configuration.gateway;

import com.kanwise.api_gateway.filter.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

import static java.time.Duration.ofMillis;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RequiredArgsConstructor
@Configuration
public class GatewayConfiguration {

    private final AuthenticationFilter authenticationFilter;

    @Bean
    public RouteLocator gateway(RouteLocatorBuilder routeLocatorBuilder) {
        String fallbackUri = "/fallback";
        return routeLocatorBuilder.routes()
                .route(routeSpec -> routeSpec
                        .path("/auth/**", "/user/**", "/image/**")
                        .filters(f -> f.filter(authenticationFilter).circuitBreaker(c -> c.setName("userCB").setFallbackUri(fallbackUri))
                                .retry(getRetryConfig()))
                        .uri("lb://user-service"))
                .route(routeSpec -> routeSpec
                        .path("/project/**", "/task/**", "/member/**", "/join/**")
                        .filters(f -> f.filter(authenticationFilter).circuitBreaker(c -> c.setName("kanwiseCB").setFallbackUri(fallbackUri))
                                .retry(getRetryConfig()))
                        .uri("lb://kanwise-service"))
                .route(routeSpec -> routeSpec
                        .path("/subscriber/**", "/job/**")
                        .filters(f -> f.filter(authenticationFilter).circuitBreaker(c -> c.setName("reportCB").setFallbackUri(fallbackUri))
                                .retry(getRetryConfig()))
                        .uri("lb://report-service"))
                .build();
    }

    private Consumer<RetryGatewayFilterFactory.RetryConfig> getRetryConfig() {
        return config -> config
                .setRetries(3)
                .setBackoff(ofMillis(300), null, 3, false)
                .setMethods(GET, POST, PUT, PATCH, DELETE)
                .setStatuses(BAD_GATEWAY, GATEWAY_TIMEOUT, INTERNAL_SERVER_ERROR);
    }
}
