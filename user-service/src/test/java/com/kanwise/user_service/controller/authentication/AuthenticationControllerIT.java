package com.kanwise.user_service.controller.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.user_service.configuration.security.jwt.JwtConfigurationProperties;
import com.kanwise.user_service.model.authentication.request.LoginRequest;
import com.kanwise.user_service.model.error.ValidationErrorDto;
import com.kanwise.user_service.model.jwt.TokenValidationRequest;
import com.kanwise.user_service.model.response.HttpResponse;
import com.kanwise.user_service.test.DatabaseCleaner;
import com.kanwise.user_service.test.TestPayload;
import com.kanwise.user_service.utils.JwtTestToken;
import liquibase.exception.LiquibaseException;
import org.apache.kafka.clients.admin.AdminClient;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static com.kanwise.user_service.constant.SecurityConstant.TOKEN_CANNOT_BE_VERIFIED;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.apache.kafka.clients.admin.AdminClient.create;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Named.of;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.kafka.config.TopicBuilder.name;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.utility.DockerImageName.parse;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@Testcontainers
class AuthenticationControllerIT {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(parse("confluentinc/cp-kafka:latest"));
    private final MockMvc mockMvc;
    private final JwtConfigurationProperties jwtConfigurationProperties;
    private final DatabaseCleaner databaseCleaner;
    private final ObjectMapper objectMapper;
    private final AdminClient kafkaAdminClient;


    @Autowired
    AuthenticationControllerIT(MockMvc mockMvc, JwtConfigurationProperties jwtConfigurationProperties, DatabaseCleaner databaseCleaner, ObjectMapper objectMapper, KafkaAdmin kafkaAdmin) {
        this.mockMvc = mockMvc;
        this.jwtConfigurationProperties = jwtConfigurationProperties;
        this.databaseCleaner = databaseCleaner;
        this.objectMapper = objectMapper;
        this.kafkaAdminClient = create(kafkaAdmin.getConfigurationProperties());
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        kafkaAdminClient.createTopics(List.of(name("notification-sms").build(), name("notification-email").build()));
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        kafkaAdminClient.deleteTopics(List.of("notification-sms", "notification-email"));
        kafkaAdminClient.close();
        databaseCleaner.cleanUp();
    }

    @DisplayName("Should validate token and return user data")
    @Nested
    class ShouldValidateToken {
        @Test
        void shouldValidateTokenAndReturnUserData() throws Exception {
            // Given
            String username = "jargrave0";
            String[] authorities = {};
            JwtTestToken jwtTestToken = new JwtTestToken(username, authorities, jwtConfigurationProperties);
            String token = jwtTestToken.generateTokenAsString();
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(token);
            // When
            // Then
            mockMvc.perform(post("/auth/token/validate")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
        }
    }

    @DisplayName("Should not validate token with invalid input parameters")
    @Nested
    class ShouldNotValidateTokenWithInvalidInputParameters {
        static Stream<Arguments> shouldNotLoginWithBlankTokenArguments() {
            return Stream.of(Arguments.of(of("Should not validate blank token | (token = \"\")",
                            TestPayload.builder()
                                    .payload(new TokenValidationRequest(""))
                                    .expectedMessage("TOKEN_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not validate token with blank token | (token = \"\\s\")",
                            TestPayload.builder()
                                    .payload(new TokenValidationRequest("  "))
                                    .expectedMessage("TOKEN_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not validate token with blank token | (token = \"\\t\")",
                            TestPayload.builder()
                                    .payload(new TokenValidationRequest(" \t"))
                                    .expectedMessage("TOKEN_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not validate token with blank token | (token = \"\\n\")",
                            TestPayload.builder()
                                    .payload(new TokenValidationRequest(" \n"))
                                    .expectedMessage("TOKEN_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not validate token with blank token | (token = \"\\r\")",
                            TestPayload.builder()
                                    .payload(new TokenValidationRequest(" \r"))
                                    .expectedMessage("TOKEN_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not validate token with blank token | (token = \"\\f\")",
                            TestPayload.builder()
                                    .payload(new TokenValidationRequest(" \f"))
                                    .expectedMessage("TOKEN_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not validate token with blank token | (token = \"\\u000B\")",
                            TestPayload.builder()
                                    .payload(new TokenValidationRequest(" \u000B"))
                                    .expectedMessage("TOKEN_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not validate blank token | (token = null)",
                            TestPayload.builder()
                                    .payload(new TokenValidationRequest(null))
                                    .expectedMessage("TOKEN_NOT_BLANK")
                                    .build())));
        }

        @ParameterizedTest
        @MethodSource("shouldNotLoginWithBlankTokenArguments")
        void shouldNotValidateTokenWithBlankToken(TestPayload<TokenValidationRequest> testPayload) throws Exception {
            // Given
            TokenValidationRequest tokenValidationRequest = testPayload.payload();
            String expectedMessage = testPayload.expectedMessage();
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/token/validate")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'token' && @.message == '%s')]", expectedMessage).exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            Assertions.assertEquals(1, errors.size());
        }
    }

    @DisplayName("Should not validate token with invalid token")
    @Nested
    class ShouldNotValidateTokenWithInvalidToken {
        @Test
        void shouldNotValidateTokenWithInvalidSecretKey() throws Exception {
            // Given
            String username = "jargrave0";
            String[] authorities = {};
            String invalidSecretKey = "invalidSecretKey";
            JwtTestToken jwtTestToken = new JwtTestToken(username, authorities, jwtConfigurationProperties);
            jwtTestToken.setSecretKey(invalidSecretKey);
            String invalidToken = jwtTestToken.generateTokenAsString();
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(invalidToken);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/token/validate")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(UNAUTHORIZED.value()))
                    .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value(TOKEN_CANNOT_BE_VERIFIED))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            HttpResponse response = objectMapper.readValue(responseJson, HttpResponse.class);
            assertThat(response.timestamp(), within(1, MINUTES, now()));
            assertNotEquals(jwtConfigurationProperties.secretKey(), invalidSecretKey);
        }

        @Test
        void shouldNotValidateTokenWithInvalidIssuer() throws Exception {
            // Given
            String username = "jargrave0";
            String[] authorities = {};
            String invalidIssuer = "invalidIssuer";
            JwtTestToken jwtTestToken = new JwtTestToken(username, authorities, jwtConfigurationProperties);
            jwtTestToken.setIssuer(invalidIssuer);
            String invalidToken = jwtTestToken.generateTokenAsString();
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(invalidToken);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/token/validate")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(UNAUTHORIZED.value()))
                    .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value(TOKEN_CANNOT_BE_VERIFIED))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            HttpResponse response = objectMapper.readValue(responseJson, HttpResponse.class);
            assertThat(response.timestamp(), within(1, MINUTES, now()));
            assertNotEquals(jwtConfigurationProperties.issuer(), invalidIssuer);
        }

        @Test
        void shouldNotValidateTokenWithInvalidAudience() throws Exception {
            // Given
            String username = "jargrave0";
            String[] authorities = {};
            String invalidAudience = "invalidAudience";
            JwtTestToken jwtTestToken = new JwtTestToken(username, authorities, jwtConfigurationProperties);
            jwtTestToken.setAudience(invalidAudience);
            String invalidToken = jwtTestToken.generateTokenAsString();
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(invalidToken);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/token/validate")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(UNAUTHORIZED.value()))
                    .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value(TOKEN_CANNOT_BE_VERIFIED))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            HttpResponse response = objectMapper.readValue(responseJson, HttpResponse.class);
            assertThat(response.timestamp(), within(1, MINUTES, now()));
            assertNotEquals(jwtConfigurationProperties.audience(), invalidAudience);
        }

        @Test
        void shouldNotValidateTokenWithExpiredToken() throws Exception {
            // Given
            String username = "jargrave0";
            String[] authorities = {};
            JwtTestToken jwtTestToken = new JwtTestToken(username, authorities, jwtConfigurationProperties);
            Date expectedExpiration = jwtTestToken.getExpiration();
            Date pastDate = Date.from(now().minusMinutes(1).atZone(ZoneId.systemDefault()).toInstant());
            jwtTestToken.setExpiration(pastDate);
            String expiredToken = jwtTestToken.generateTokenAsString();
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(expiredToken);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/token/validate")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(UNAUTHORIZED.value()))
                    .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value(TOKEN_CANNOT_BE_VERIFIED))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            HttpResponse response = objectMapper.readValue(responseJson, HttpResponse.class);
            assertThat(response.timestamp(), within(1, MINUTES, now()));
            assertThat(pastDate, Matchers.lessThan(expectedExpiration));
        }
    }

    @DisplayName("Should not validate token with invalid user")
    @Nested
    class ShouldNotValidateTokenWithInvalidUser {
        @Test
        void shouldNotValidateTokenWithNonExistingUser() throws Exception {
            // Given
            String username = "nonExistingUsername";
            String[] authorities = {};
            JwtTestToken jwtTestToken = new JwtTestToken(username, authorities, jwtConfigurationProperties);
            String invalidToken = jwtTestToken.generateTokenAsString();
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(invalidToken);
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, "password"))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"));
            // Then
            String responseJson = mockMvc.perform(post("/auth/token/validate")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            HttpResponse response = objectMapper.readValue(responseJson, HttpResponse.class);
            assertThat(response.timestamp(), within(1, MINUTES, now()));
        }

        @Test
        void shouldNotValidateTokenWithDisabledUser() throws Exception {
            // Given
            String username = "abeaston1d";
            String[] authorities = {};
            JwtTestToken jwtTestToken = new JwtTestToken(username, authorities, jwtConfigurationProperties);
            String invalidToken = jwtTestToken.generateTokenAsString();
            TokenValidationRequest tokenValidationRequest = new TokenValidationRequest(invalidToken);
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, "password"))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("USER_IS_DISABLED"));
            // Then
            String responseJson = mockMvc.perform(post("/auth/token/validate")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tokenValidationRequest)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("USER_IS_DISABLED"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            HttpResponse response = objectMapper.readValue(responseJson, HttpResponse.class);
            assertThat(response.timestamp(), within(1, MINUTES, now()));
        }
    }
}