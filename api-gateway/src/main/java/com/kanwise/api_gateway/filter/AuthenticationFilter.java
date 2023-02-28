package com.kanwise.api_gateway.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.api_gateway.configuration.security.SecurityConfigurationProperties;
import com.kanwise.api_gateway.excpetion.custom.GatewayAuthorizationException;
import com.kanwise.api_gateway.excpetion.custom.GatewayCommunicationException;
import com.kanwise.api_gateway.model.http.response.HttpResponse;
import com.kanwise.api_gateway.model.token.TokenValidationRequest;
import com.kanwise.api_gateway.model.user.UserDto;
import com.kanwise.api_gateway.validator.RouterValidator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.function.Function;

import static com.kanwise.api_gateway.model.http.custom.HttpHeader.ROLE;
import static com.kanwise.api_gateway.model.http.custom.HttpHeader.USERNAME;
import static com.kanwise.api_gateway.model.http.response.ErrorMessage.AUTHENTICATION_HEADER_IS_NOT_PRESENT;
import static com.kanwise.api_gateway.model.http.response.ErrorMessage.TOKEN_IS_NOT_VALID;
import static com.kanwise.api_gateway.model.token.custom.TokenPrefix.BEARER;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static reactor.core.publisher.Flux.just;

@RequiredArgsConstructor
@RefreshScope
@Component
public class AuthenticationFilter implements GatewayFilter {
    private final RouterValidator routerValidator;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final SecurityConfigurationProperties securityConfigurationProperties;
    private final Clock clock;

    private static boolean isTokenValid(String[] tokenParts) {
        return tokenParts.length == 2 && tokenParts[0].equals(BEARER);
    }

    private static Function<UserDto, ServerWebExchange> mutateExchangeWithUserDto(ServerWebExchange exchange) {
        return userDto -> {
            exchange.getRequest()
                    .mutate()
                    .header(USERNAME, userDto.username())
                    .header(ROLE, userDto.userRole())
                    .build();
            return exchange;
        };
    }

    private static Mono<? extends Throwable> handle4xxServerError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(HttpResponse.class).map(GatewayAuthorizationException::new);
    }

    private static Mono<? extends Throwable> handle5xxServerError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class).map(responseJson -> {
            try {
                HttpResponse httpResponse = new ObjectMapper().readValue(responseJson, HttpResponse.class);
                return new GatewayCommunicationException(httpResponse);
            } catch (Exception e) {
                return new GatewayCommunicationException(responseJson);
            }
        });
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (isRouterValidatorSecured(request)) {
            return validateAuthentication(exchange, chain, request);
        }
        return chain.filter(exchange);
    }

    private Mono<Void> validateAuthentication(ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest request) {
        if (!isAuthenticationHeaderPresent(request)) {
            return onError(exchange, UNAUTHORIZED, AUTHENTICATION_HEADER_IS_NOT_PRESENT);
        }

        String[] tokenParts = getAuthenticationTokenParts(request);

        if (!isTokenValid(tokenParts)) {
            return onError(exchange, UNAUTHORIZED, TOKEN_IS_NOT_VALID);
        }

        return validateAuthenticationExternally(exchange, chain, tokenParts);
    }

    private Mono<Void> validateAuthenticationExternally(ServerWebExchange exchange, GatewayFilterChain chain, String[] tokenParts) {
        return getRequestBodySpec(securityConfigurationProperties.authenticationRoute(), POST)
                .contentType(APPLICATION_JSON)
                .bodyValue(new TokenValidationRequest(tokenParts[1]))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, AuthenticationFilter::handle4xxServerError)
                .onStatus(HttpStatus::is5xxServerError, AuthenticationFilter::handle5xxServerError)
                .bodyToMono(UserDto.class)
                .map(mutateExchangeWithUserDto(exchange))
                .flatMap(chain::filter)
                .onErrorResume(GatewayAuthorizationException.class, e -> onError(exchange, UNAUTHORIZED, e.getMessage()));
    }

    private boolean isRouterValidatorSecured(ServerHttpRequest request) {
        return routerValidator.isRouteSecured(request);
    }

    private String[] getAuthenticationTokenParts(ServerHttpRequest request) {
        return getAuthenticationHeader(request).split(" ");
    }

    private String getAuthenticationHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty(AUTHORIZATION).get(0);
    }

    private boolean isAuthenticationHeaderPresent(ServerHttpRequest request) {
        return request.getHeaders().containsKey(AUTHORIZATION);
    }

    private WebClient.RequestBodySpec getRequestBodySpec(String uri, HttpMethod method) {
        return webClientBuilder.build().method(method).uri(uri);
    }


    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        return response.writeWith(just(getResponseDataBuffer(httpStatus, message, response)));
    }

    private DataBuffer getResponseDataBuffer(HttpStatus httpStatus, String message, ServerHttpResponse response) {
        return response.bufferFactory().wrap(generateResponseAsBytes(httpStatus, message));
    }


    @SneakyThrows
    private byte[] generateResponseAsBytes(HttpStatus httpStatus, String message) {
        return objectMapper.writeValueAsBytes(HttpResponse.builder()
                .timestamp(now(clock))
                .httpStatusCode(httpStatus.value())
                .httpStatus(httpStatus)
                .reason(httpStatus.getReasonPhrase())
                .message(message)
                .build());
    }
}

