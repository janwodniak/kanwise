package com.kanwise.user_service.controller.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.clients.kanwise_service.member.MemberClient;
import com.kanwise.clients.kanwise_service.member.model.MemberDto;
import com.kanwise.user_service.configuration.security.brute_force_attack.BruteForceAttackConfigurationProperties;
import com.kanwise.user_service.model.authentication.request.LoginRequest;
import com.kanwise.user_service.model.error.ValidationErrorDto;
import com.kanwise.user_service.model.response.HttpResponse;
import com.kanwise.user_service.test.DatabaseCleaner;
import com.kanwise.user_service.test.TestPayload;
import liquibase.exception.LiquibaseException;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.stream.Stream;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.apache.kafka.clients.admin.AdminClient.create;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Named.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.kafka.config.TopicBuilder.name;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.utility.DockerImageName.parse;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@Testcontainers
class LoginControllerIT {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(parse("confluentinc/cp-kafka:latest"));
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DatabaseCleaner databaseCleaner;
    private final BruteForceAttackConfigurationProperties bruteForceAttackConfigurationProperties;
    private final AdminClient kafkaAdminClient;
    @MockBean
    private MemberClient memberClient;


    @Autowired
    public LoginControllerIT(MockMvc mockMvc, ObjectMapper objectMapper, DatabaseCleaner databaseCleaner, BruteForceAttackConfigurationProperties bruteForceAttackConfigurationProperties, KafkaAdmin kafkaAdmin) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.databaseCleaner = databaseCleaner;
        this.bruteForceAttackConfigurationProperties = bruteForceAttackConfigurationProperties;
        this.kafkaAdminClient = create(kafkaAdmin.getConfigurationProperties());

    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        kafkaAdminClient.createTopics(List.of(name("notification-sms").build(), name("notification-email").build()));
        when(memberClient.addMember(any())).thenReturn(ResponseEntity.ok(new MemberDto()));
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        kafkaAdminClient.deleteTopics(List.of("notification-sms", "notification-email"));
        kafkaAdminClient.close();
        databaseCleaner.cleanUp();
    }

    private String getValidAdminToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("celders1", "Password123*");
        return mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.firstName").value("Cully"))
                .andExpect(jsonPath("$.lastName").value("Elders"))
                .andExpect(jsonPath("$.username").value(loginRequest.username()))
                .andExpect(jsonPath("$.email").value("cullyelders.kanwise@gmail.com"))
                .andExpect(jsonPath("$.userRole").value("ADMIN"))
                .andExpect(jsonPath("$.lastLoginDate").exists())
                .andExpect(jsonPath("$.joinDate").exists())
                .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                .andExpect(jsonPath("$.phoneNumber").value("+54 508 521 2350"))
                .andExpect(header().exists(AUTHORIZATION))
                .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")))
                .andReturn()
                .getResponse()
                .getHeader(AUTHORIZATION);
    }

    @DisplayName("Should login")
    @Nested
    class ShouldLogin {

        @Test
        void shouldLogin() throws Exception {
            // Given
            LoginRequest loginRequest = new LoginRequest("jargrave0", "Password123*");
            // When
            mockMvc.perform(get("/user/1")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                            .header(AUTHORIZATION, getValidAdminToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(loginRequest.username()))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
            // Then
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(loginRequest.username()))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
        }
    }

    @DisplayName("Should not login")
    @Nested
    class ShouldNotLogin {

        static Stream<Arguments> shouldNotLoginWithBlankUsernameArguments() {
            return Stream.of(Arguments.of(of("Should not login with blank username | (username = \"\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("", "Password123*"))
                                    .expectedMessage("USERNAME_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with blank username | (username = \"\\s\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest(" ", "Password123*"))
                                    .expectedMessage("USERNAME_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with blank username | (username = \"\\t\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("\t", "Password123*"))
                                    .expectedMessage("USERNAME_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with blank username | (username = \"\\n\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("\n", "Password123*"))
                                    .expectedMessage("USERNAME_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with blank username | (username = \"\\r\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("\r", "Password123*"))
                                    .expectedMessage("USERNAME_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with blank username | (username = \"\\f\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("\f", "Password123*"))
                                    .expectedMessage("USERNAME_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with blank username | (username = \"\\u000B\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("\u000B", "Password123*"))
                                    .expectedMessage("USERNAME_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with null username | (username = null)",
                            TestPayload.builder()
                                    .payload(new LoginRequest(null, "Password123*"))
                                    .expectedMessage("USERNAME_NOT_BLANK")
                                    .build())));
        }

        static Stream<Arguments> shouldNotLoginWithBlankPasswordArguments() {
            return Stream.of(Arguments.of(of("Should not login with blank password | (password = \"\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("jargrave0", ""))
                                    .expectedMessage("PASSWORD_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with blank password | (password = \"\\s\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("jargrave0", " "))
                                    .expectedMessage("PASSWORD_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with blank password | (password = \"\\t\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("jargrave0", " \t"))
                                    .expectedMessage("PASSWORD_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with blank password | (password = \"\\n\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("jargrave0", " \n"))
                                    .expectedMessage("PASSWORD_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with blank password | (password = \"\\r\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("jargrave0", " \r"))
                                    .expectedMessage("PASSWORD_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with blank password | (password = \"\\f\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("jargrave0", " \f"))
                                    .expectedMessage("PASSWORD_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with blank password | (password = \"\\u000B\")",
                            TestPayload.builder()
                                    .payload(new LoginRequest("jargrave0", " \u000B"))
                                    .expectedMessage("PASSWORD_NOT_BLANK")
                                    .build())),
                    Arguments.of(of("Should not login with null password | (password = null)",
                            TestPayload.builder()
                                    .payload(new LoginRequest("jargrave0", null))
                                    .expectedMessage("PASSWORD_NOT_BLANK")
                                    .build()))
            );
        }

        @DisplayName("Should not login with blank username")
        @MethodSource("shouldNotLoginWithBlankUsernameArguments")
        @ParameterizedTest
        void shouldNotLoginWithBlankUsername(TestPayload<LoginRequest> testPayload) throws Exception {
            // Given
            LoginRequest loginRequest = testPayload.payload();
            String expectedMessage = testPayload.expectedMessage();
            // When
            String responseJson = mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == '%s')]", expectedMessage).exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            assertEquals(1, errors.size());
        }

        @DisplayName("Should not login with blank password")
        @MethodSource("shouldNotLoginWithBlankPasswordArguments")
        @ParameterizedTest
        void shouldNotLoginWithBlankPassword(TestPayload<LoginRequest> testPayload) throws Exception {

            // Given
            LoginRequest loginRequest = testPayload.payload();
            String expectedMessage = testPayload.expectedMessage();
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'password' && @.message == '%s')]", expectedMessage).exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            assertEquals(1, errors.size());
        }
    }

    @DisplayName("Should block user after defined number of failed login attempts in order to prevent brute force attacks")
    @Nested
    class ShouldBlockUser {
        @DisplayName("Should block user after 5 failed login attempts")
        @Test
        void shouldBlockUserAfter5FailedLoginAttempts() throws Exception {
            // Given
            String invalidPassword = "invalidPassword";
            int loginAttempts = 5;
            LoginRequest loginRequest = new LoginRequest("jargrave0", invalidPassword);
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getValidAdminToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(loginRequest.username()))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));

            for (int i = 0; i < loginAttempts; i++) {
                String responseJson = mockMvc.perform(post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                        .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                        .andExpect(jsonPath("$.message").value("BAD_CREDENTIALS"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                HttpResponse response = objectMapper.readValue(responseJson, HttpResponse.class);
                assertThat(response.timestamp(), within(1, MINUTES, now()));
            }
            // Then
            String responseJson = mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("USER_ACCOUNT_IS_LOCKED"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            HttpResponse response = objectMapper.readValue(responseJson, HttpResponse.class);
            assertThat(response.timestamp(), within(1, MINUTES, now()));
            assertThat(loginAttempts, greaterThanOrEqualTo(bruteForceAttackConfigurationProperties.maximumNumberOfAttempts()));
        }
    }

    @DisplayName("Should not block user if configured limit were not reached")
    @Nested
    class ShouldNotBlockUser {
        @DisplayName("Should not block user if configured limits were not reached | (limit = 5, attempts = 4)")
        @Test
        void shouldNotBlockUserIfNumberOfUnsuccessfulLoginAttemptsIsLessThanTheGivenLimit() throws Exception {
            // Given
            String username = "jargrave0";
            int loginAttempts = 4;
            LoginRequest invalidLoginRequest = new LoginRequest(username, "invalidPassword");
            LoginRequest validLoginRequest = new LoginRequest(username, "Password123*");
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getValidAdminToken()))
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

            for (int i = 0; i < loginAttempts; i++) {
                String responseJson = mockMvc.perform(post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                        .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                        .andExpect(jsonPath("$.message").value("BAD_CREDENTIALS"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                HttpResponse response = objectMapper.readValue(responseJson, HttpResponse.class);
                assertThat(response.timestamp(), within(1, MINUTES, now()));
            }
            // Then
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(validLoginRequest.username()))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));

            assertThat(loginAttempts, lessThan(bruteForceAttackConfigurationProperties.maximumNumberOfAttempts()));
        }
    }
}
