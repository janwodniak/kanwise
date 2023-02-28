package com.kanwise.api_gateway.validator;

import com.kanwise.api_gateway.configuration.security.SecurityConfigurationProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.patch;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.put;

@ActiveProfiles("test")
class RouterValidatorTest {

    private SecurityConfigurationProperties getSecurityConfigurationProperties() {
        return SecurityConfigurationProperties.builder()
                .openApiEndpoints(
                        of(
                                "/auth/register",
                                "/auth/login",
                                "/auth/password/reset/request",
                                "/auth/password/request/forgotten",
                                "/auth/password/reset/forgotten",
                                "/auth/password/reset",
                                "/auth/registration/otp/sms")
                )
                .build();
    }

    @DisplayName("Should return false if route is not secured")
    @Nested
    class ShouldValidateOpenRoutes {

        static List<ServerHttpRequest> securedRoutesArguments() {
            return of(
                    get("/auth/register").build(),
                    get("/auth/login").build(),
                    get("/auth/password/reset/request").build(),
                    post("/auth/password/request/forgotten").build(),
                    post("/auth/password/reset/forgotten").build(),
                    post("/auth/password/reset").build(),
                    patch("/auth/registration/otp/sms").build(),
                    patch("/auth/register/v1").build(),
                    patch("/auth/login/v3").build(),
                    put("/auth/password/reset/request?user=123").build(),
                    put("/auth/password/request/forgotten?type=mail").build());
        }

        @MethodSource("securedRoutesArguments")
        @ParameterizedTest
        void shouldValidateOpenRouted(ServerHttpRequest request) {
            // Given
            RouterValidator routerValidator = new RouterValidator(getSecurityConfigurationProperties());
            // When
            // Then
            assertFalse(routerValidator.isRouteSecured(request));
        }
    }

    @DisplayName("Should return true if route is secured")
    @Nested
    class SecuredRoutes {

        static List<ServerHttpRequest> securedRoutesArguments() {
            return of(
                    get("/auth/user").build(),
                    get("/project/1/member/123").build(),
                    get("/api/v1/project/1/member/123").build(),
                    get("/api/v1/project?user=123").build()
            );
        }

        @MethodSource("securedRoutesArguments")
        @ParameterizedTest
        void shouldReturnTrueIfRouteIsSecured(ServerHttpRequest request) {
            // Given
            RouterValidator routerValidator = new RouterValidator(getSecurityConfigurationProperties());
            // When
            // Then
            assertTrue(routerValidator.isRouteSecured(request));
        }
    }
}