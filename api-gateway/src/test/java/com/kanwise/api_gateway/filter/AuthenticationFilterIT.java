package com.kanwise.api_gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.kanwise.api_gateway.model.http.response.HttpResponse;
import com.kanwise.api_gateway.model.token.TokenValidationRequest;
import com.kanwise.api_gateway.model.user.UserDto;
import com.kanwise.api_gateway.test.TestServiceInstanceListSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.unauthorized;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.kanwise.api_gateway.model.http.response.ErrorMessage.AUTHENTICATION_HEADER_IS_NOT_PRESENT;
import static com.kanwise.api_gateway.model.http.response.ErrorMessage.TOKEN_IS_NOT_VALID;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;


@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class AuthenticationFilterIT {

    private final WebTestClient webTestClient;
    private final WireMockServer wireMockServer;
    private final ObjectMapper objectMapper;

    @Autowired
    AuthenticationFilterIT(WebTestClient webTestClient, WireMockServer wireMockServer, ObjectMapper objectMapper) {
        this.webTestClient = webTestClient;
        this.wireMockServer = wireMockServer;
        this.objectMapper = objectMapper;
    }

    @AfterEach
    void afterEach() {
        wireMockServer.resetAll();
    }

    @TestConfiguration
    static class RequestHashingFilterTestConfig {
        @Bean(destroyMethod = "stop")
        WireMockServer wireMockServer() {
            WireMockConfiguration options = wireMockConfig().dynamicPort();
            WireMockServer wireMock = new WireMockServer(options);
            wireMock.start();
            return wireMock;
        }

        @Bean
        TestServiceInstanceListSupplier testServiceInstanceListSupplier(WireMockServer wireMockServer) {
            return new TestServiceInstanceListSupplier("user-service", wireMockServer.port());
        }
    }

    @DisplayName("Should validate authentication internally")
    @Nested
    class ShouldValidateAuthenticationInternally {

        @DisplayName("Should return 401 when authentication header is missing")
        @Test
        void shouldReturn401WhenAuthenticationHeaderIsMissing() {
            // Given
            // When
            // Then
            webTestClient.get()
                    .uri("/user")
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectBody()
                    .jsonPath("$.timestamp").exists()
                    .jsonPath("$.httpStatusCode").isEqualTo(UNAUTHORIZED.value())
                    .jsonPath("$.httpStatus").isEqualTo("UNAUTHORIZED")
                    .jsonPath("$.reason").isEqualTo("Unauthorized")
                    .jsonPath("$.message").isEqualTo(AUTHENTICATION_HEADER_IS_NOT_PRESENT);
        }

        @DisplayName("Should return 401 when authentication header contains only 'Bearer' part")
        @Test
        void shouldReturn401WhenAuthenticationHeaderContainsOnlyBearerPart() {
            // Given
            String invalidToken = "Bearer";
            // When
            // Then
            webTestClient.get()
                    .uri("/user")
                    .header(AUTHORIZATION, invalidToken)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectBody()
                    .jsonPath("$.timestamp").exists()
                    .jsonPath("$.httpStatusCode").isEqualTo(UNAUTHORIZED.value())
                    .jsonPath("$.httpStatus").isEqualTo("UNAUTHORIZED")
                    .jsonPath("$.reason").isEqualTo("Unauthorized")
                    .jsonPath("$.message").isEqualTo(TOKEN_IS_NOT_VALID);
        }

        @DisplayName("Should return 401 when authentication header contains only 'Bearer ' part")
        @Test
        void shouldReturn401WhenAuthenticationHeaderContainsOnlyBearerSpacePart() {
            // Given
            String invalidToken = "Bearer ";
            // When
            // Then
            webTestClient.get()
                    .uri("/user")
                    .header(AUTHORIZATION, invalidToken)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectBody()
                    .jsonPath("$.timestamp").exists()
                    .jsonPath("$.httpStatusCode").isEqualTo(UNAUTHORIZED.value())
                    .jsonPath("$.httpStatus").isEqualTo("UNAUTHORIZED")
                    .jsonPath("$.reason").isEqualTo("Unauthorized")
                    .jsonPath("$.message").isEqualTo(TOKEN_IS_NOT_VALID);

            assertEquals("Bearer", invalidToken.split(" ")[0]);
            assertEquals(1, invalidToken.split(" ").length);
            wireMockServer.verify(0, postRequestedFor(urlEqualTo("/auth/token/validate")));
        }

        @DisplayName("Should return 401 when authentication header contains invalid 'Bearer' part")
        @Test
        void shouldReturn401WhenAuthenticationHeaderContainsInvalidBearerPart() {
            // Given
            String invalidToken = "bearer token";
            // When
            // Then
            webTestClient.get()
                    .uri("/user")
                    .header(AUTHORIZATION, invalidToken)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectBody()
                    .jsonPath("$.timestamp").exists()
                    .jsonPath("$.httpStatusCode").isEqualTo(UNAUTHORIZED.value())
                    .jsonPath("$.httpStatus").isEqualTo("UNAUTHORIZED")
                    .jsonPath("$.reason").isEqualTo("Unauthorized")
                    .jsonPath("$.message").isEqualTo(TOKEN_IS_NOT_VALID);

            assertNotEquals("Bearer", invalidToken.split(" ")[0]);
            wireMockServer.verify(0, postRequestedFor(urlEqualTo("/auth/token/validate")));
        }

        @DisplayName("Should return 401 when authentication header does not contain 'Bearer' part")
        @Test
        void shouldReturn401WhenAuthenticationHeaderDoesNotContainBearerPart() {
            // Given
            String invalidToken = " token";
            // When
            // Then
            webTestClient.get()
                    .uri("/user")
                    .header(AUTHORIZATION, invalidToken)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectBody()
                    .jsonPath("$.timestamp").exists()
                    .jsonPath("$.httpStatusCode").isEqualTo(UNAUTHORIZED.value())
                    .jsonPath("$.httpStatus").isEqualTo("UNAUTHORIZED")
                    .jsonPath("$.reason").isEqualTo("Unauthorized")
                    .jsonPath("$.message").isEqualTo(TOKEN_IS_NOT_VALID);

            assertNotEquals("Bearer", invalidToken.split(" ")[0]);
            wireMockServer.verify(0, postRequestedFor(urlEqualTo("/auth/token/validate")));
        }
    }

    @DisplayName("Should validate authentication externally by calling user-service")
    @Nested
    class ShouldValidateAuthenticationExternally {

        @DisplayName("Should return 401 when authentication header is invalid")
        @Test
        void shouldReturn401WhenAuthenticationHeaderIsInvalid() throws Exception {
            // Given
            String invalidToken = "Bearer invalidToken";
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(invalidToken.split(" ")[1]);
            String expectedMessage = "TOKEN_IS_NOT_VALID";
            HttpResponse httpResponse = HttpResponse.builder()
                    .httpStatusCode(UNAUTHORIZED.value())
                    .httpStatus(UNAUTHORIZED)
                    .reason("Unauthorized")
                    .message(expectedMessage)
                    .build();
            // When
            wireMockServer.stubFor(post("/auth/token/validate")
                    .willReturn(unauthorized()
                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                            .withBody(objectMapper.writeValueAsString(httpResponse))));
            // Then
            webTestClient.get()
                    .uri("/user")
                    .header(AUTHORIZATION, invalidToken)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectBody()
                    .jsonPath("$.timestamp").exists()
                    .jsonPath("$.httpStatusCode").isEqualTo(UNAUTHORIZED.value())
                    .jsonPath("$.httpStatus").isEqualTo("UNAUTHORIZED")
                    .jsonPath("$.reason").isEqualTo("Unauthorized")
                    .jsonPath("$.message").isEqualTo(expectedMessage);

            wireMockServer.verify(1, postRequestedFor(urlEqualTo("/auth/token/validate"))
                    .withRequestBody(equalToJson(objectMapper.writeValueAsString(tokenValidationRequest))));
        }

        @DisplayName("Should return 401 when authentication header is expired")
        @Test
        void shouldReturn401WhenAuthenticationHeaderIsExpired() throws Exception {
            // Given
            String expiredToken = "Bearer expiredToken";
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(expiredToken.split(" ")[1]);
            String expectedMessage = "TOKEN_IS_EXPIRED";
            HttpResponse httpResponse = HttpResponse.builder()
                    .httpStatusCode(UNAUTHORIZED.value())
                    .httpStatus(UNAUTHORIZED)
                    .reason("Unauthorized")
                    .message(expectedMessage)
                    .build();
            // When
            wireMockServer.stubFor(post("/auth/token/validate")
                    .willReturn(unauthorized()
                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                            .withBody(objectMapper.writeValueAsString(httpResponse))));
            // Then
            webTestClient.get()
                    .uri("/user")
                    .header(AUTHORIZATION, expiredToken)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectBody()
                    .jsonPath("$.timestamp").exists()
                    .jsonPath("$.httpStatusCode").isEqualTo(UNAUTHORIZED.value())
                    .jsonPath("$.httpStatus").isEqualTo("UNAUTHORIZED")
                    .jsonPath("$.reason").isEqualTo("Unauthorized")
                    .jsonPath("$.message").isEqualTo(expectedMessage);

            wireMockServer.verify(1, postRequestedFor(urlEqualTo("/auth/token/validate"))
                    .withRequestBody(equalToJson(objectMapper.writeValueAsString(tokenValidationRequest))));
        }

        @DisplayName("Should return 500 when authentication service returns 500 as HttpResponse entity")
        @Test
        void shouldReturn500WhenServiceReturns500AsHttpResponse() throws Exception {
            // Given
            String token = "Bearer tokenPart";
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(token.split(" ")[1]);
            String expectedMessage = "SERVICE_UNAVAILABLE";
            HttpResponse internalServerErrorResponse = HttpResponse.builder()
                    .httpStatusCode(INTERNAL_SERVER_ERROR.value())
                    .httpStatus(INTERNAL_SERVER_ERROR)
                    .reason("Internal Server Error")
                    .message(expectedMessage)
                    .build();
            // When
            wireMockServer.stubFor(post("/auth/token/validate")
                    .willReturn(serverError()
                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                            .withBody(objectMapper.writeValueAsString(internalServerErrorResponse))));
            // Then
            webTestClient.get()
                    .uri("/user")
                    .header(AUTHORIZATION, token)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.timestamp").exists()
                    .jsonPath("$.httpStatusCode").isEqualTo(INTERNAL_SERVER_ERROR.value())
                    .jsonPath("$.httpStatus").isEqualTo("INTERNAL_SERVER_ERROR")
                    .jsonPath("$.reason").isEqualTo("Internal Server Error")
                    .jsonPath("$.message").isEqualTo(expectedMessage);

            wireMockServer.verify(1, postRequestedFor(urlEqualTo("/auth/token/validate"))
                    .withRequestBody(equalToJson(objectMapper.writeValueAsString(tokenValidationRequest))));
        }

        @DisplayName("Should return 500 when authentication service returns 500 as plain text")
        @Test
        void shouldReturn500WhenServiceReturns500AsPlainText() throws Exception {
            // Given
            String token = "Bearer tokenPart";
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(token.split(" ")[1]);
            String expectedMessage = "SERVICE_UNAVAILABLE";
            // When
            wireMockServer.stubFor(post("/auth/token/validate")
                    .willReturn(serverError()
                            .withHeader(CONTENT_TYPE, TEXT_PLAIN_VALUE)
                            .withBody(expectedMessage)));
            // Then
            webTestClient.get()
                    .uri("/user")
                    .header(AUTHORIZATION, token)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.timestamp").exists()
                    .jsonPath("$.httpStatusCode").isEqualTo(INTERNAL_SERVER_ERROR.value())
                    .jsonPath("$.httpStatus").isEqualTo("INTERNAL_SERVER_ERROR")
                    .jsonPath("$.reason").isEqualTo("Internal Server Error")
                    .jsonPath("$.message").isEqualTo(expectedMessage);

            wireMockServer.verify(1, postRequestedFor(urlEqualTo("/auth/token/validate"))
                    .withRequestBody(equalToJson(objectMapper.writeValueAsString(tokenValidationRequest))));
        }


        @DisplayName("Should add appropriate headers when authentication header is valid, and then pass on the request")
        @Test
        void shouldAddHeadersWhenAuthenticationHeaderIsValidAndPassThenOnTheRequest() throws Exception {
            // Given
            String token = "Bearer tokenPart";
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(token.split(" ")[1]);
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .firstName("Joletta")
                    .lastName("Tiger")
                    .username("jolettatiger")
                    .email("jolettatiger.kanwise@gmail.com")
                    .userRole("USER")
                    .lastLoginDate(LocalDateTime.now())
                    .joinDate(LocalDateTime.now().minus(1, DAYS))
                    .build();
            // When
            wireMockServer.stubFor(post("/auth/token/validate")
                    .willReturn(ok()
                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                            .withBody(objectMapper.writeValueAsString(userDto))));

            wireMockServer.stubFor(get("/user")
                    .willReturn(ok()
                            .withHeader("username", userDto.username())
                            .withHeader("role", userDto.userRole())));
            // Then
            webTestClient.get()
                    .uri("/user")
                    .header(AUTHORIZATION, token)
                    .exchange()
                    .expectStatus().isOk()
                    // todo: ROLE, USERNAME
                    .expectHeader().valueEquals("username", userDto.username())
                    .expectHeader().valueEquals("role", userDto.userRole());

            wireMockServer.verify(1, postRequestedFor(urlEqualTo("/auth/token/validate"))
                    .withRequestBody(equalToJson(objectMapper.writeValueAsString(tokenValidationRequest))));
            wireMockServer.verify(1, getRequestedFor(urlEqualTo("/user"))
                    .withHeader("username", equalTo(userDto.username()))
                    .withHeader("role", equalTo(userDto.userRole())));
        }
    }

    @DisplayName("Should not perform validation if route is not secured")
    @Nested
    class ShouldNotPerformAuthenticationValidationIfRouteIsNotSecured {

        static Stream<String> notSecuredRoutesParameters() {
            return Stream.of(
                    "/auth/register",
                    "/auth/login",
                    "/auth/password/reset/request",
                    "/auth/password/request/forgotten",
                    "/auth/password/reset/forgotten",
                    "/auth/password/reset",
                    "/auth/registration/otp/sms"
            );
        }

        @DisplayName("Should not perform validation if route is not secured and authorization header is absent")
        @MethodSource("notSecuredRoutesParameters")
        @ParameterizedTest
        void shouldNotPerformValidationIfRouteIsNotSecuredWithAbsentAuthorizationHeaderHeader(String notSecuredRoute) {
            // Given
            // When
            wireMockServer.stubFor(post("/auth/token/validate"));
            wireMockServer.stubFor(post(notSecuredRoute).willReturn(ok()));
            // Then
            webTestClient.post()
                    .uri(notSecuredRoute)
                    .exchange()
                    .expectStatus().isOk();

            wireMockServer.verify(0, postRequestedFor(urlEqualTo("/auth/token/validate")));
            wireMockServer.verify(1, postRequestedFor(urlEqualTo(notSecuredRoute)));
        }

        @DisplayName("Should not perform validation if route is not secured and authorization header is present")
        @MethodSource("notSecuredRoutesParameters")
        @ParameterizedTest
        void shouldNotPerformedValidationIfRouteIsNotSecuredWithPresentAuthorizationHeader(String notSecuredRoute) throws Exception {
            // Given
            String token = "Bearer tokenPart";
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(token.split(" ")[1]);
            // When
            wireMockServer.stubFor(post("/auth/token/validate"));
            wireMockServer.stubFor(post(notSecuredRoute).willReturn(ok()));
            // Then
            webTestClient.post()
                    .uri(notSecuredRoute)
                    .header(AUTHORIZATION, token)
                    .exchange()
                    .expectStatus().isOk();

            wireMockServer.verify(0, postRequestedFor(urlEqualTo("/auth/token/validate"))
                    .withRequestBody(equalToJson(objectMapper.writeValueAsString(tokenValidationRequest))));
            wireMockServer.verify(1, postRequestedFor(urlEqualTo(notSecuredRoute)));
        }
    }
}