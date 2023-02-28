package com.kanwise.api_gateway.controller;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.stream.Stream;

import static com.kanwise.api_gateway.model.http.response.ErrorMessage.CONNECTION_ERROR_TRY_AGAIN_LATER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class FallbackControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    static Stream<HttpMethod> shouldReturnFallbackResponseHttpMethodArguments() {
        return Stream.of(POST, PUT, GET, PATCH, DELETE);
    }

    @MethodSource("shouldReturnFallbackResponseHttpMethodArguments")
    @ParameterizedTest
    void shouldReturnFallbackResponse(HttpMethod method) {
        // Given
        // When
        // Then
        webTestClient.method(method).uri("/fallback")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.httpStatusCode").isEqualTo(INTERNAL_SERVER_ERROR.value())
                .jsonPath("$.httpStatus").isEqualTo("INTERNAL_SERVER_ERROR")
                .jsonPath("$.reason").isEqualTo(INTERNAL_SERVER_ERROR.getReasonPhrase())
                .jsonPath("$.message").isEqualTo(CONNECTION_ERROR_TRY_AGAIN_LATER);
    }
}

