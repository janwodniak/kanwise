package com.kanwise.user_service.controller.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.user_service.configuration.security.password_reset_token.PasswordResetTokenConfigurationProperties;
import com.kanwise.user_service.model.authentication.password.ForgottenPasswordResetCommand;
import com.kanwise.user_service.model.authentication.password.ForgottenPasswordResetRequest;
import com.kanwise.user_service.model.authentication.password.PasswordResetCommand;
import com.kanwise.user_service.model.authentication.request.LoginRequest;
import com.kanwise.user_service.model.error.ValidationErrorDto;
import com.kanwise.user_service.model.notification.email.EmailRequest;
import com.kanwise.user_service.model.response.HttpResponse;
import com.kanwise.user_service.test.DatabaseCleaner;
import com.kanwise.user_service.test.TestPayload;
import liquibase.exception.LiquibaseException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.kanwise.user_service.constant.SecurityConstant.ACCESS_DENIED_MESSAGE;
import static com.kanwise.user_service.controller.kafka.KafkaTestingUtils.getKafkaConsumerProperties;
import static java.time.Duration.ofMillis;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.clients.admin.AdminClient.create;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Named.of;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.kafka.config.TopicBuilder.name;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static org.testcontainers.utility.DockerImageName.parse;

@SpringBootTest
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class PasswordControllerIT {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(parse("confluentinc/cp-kafka:latest"));
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DatabaseCleaner databaseCleaner;
    private final AdminClient kafkaAdminClient;
    private final PasswordResetTokenConfigurationProperties passwordResetTokenConfigurationProperties;
    @MockBean
    private Clock clock;

    @Autowired
    public PasswordControllerIT(MockMvc mockMvc, ObjectMapper objectMapper, DatabaseCleaner databaseCleaner, KafkaAdmin kafkaAdmin, PasswordResetTokenConfigurationProperties passwordResetTokenConfigurationProperties) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.databaseCleaner = databaseCleaner;
        this.kafkaAdminClient = create(kafkaAdmin.getConfigurationProperties());
        this.passwordResetTokenConfigurationProperties = passwordResetTokenConfigurationProperties;
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());
        when(clock.instant()).thenReturn(Clock.systemDefaultZone().instant());
        kafkaAdminClient.createTopics(List.of(name("notification-sms").build(), name("notification-email").build()));
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        kafkaAdminClient.deleteTopics(List.of("notification-sms", "notification-email"));
        kafkaAdminClient.close();
        databaseCleaner.cleanUp();
    }

    @DisplayName("Should reset forgotten password")
    @Nested
    class ShouldResetForgottenPassword {

        @DisplayName("Should reset forgotten password, when request is valid, then user should receive reset token in email")
        @Test
        void shouldResetForgottenPassword() throws Exception {
            // Given
            String email = "jolettatiger.kanwise@gmail.com";
            String topicName = "notification-email";
            String oldPassword = "Password123*";
            String newPassword = "*321drowssaP";
            ForgottenPasswordResetRequest forgottenPasswordResetRequest = new ForgottenPasswordResetRequest(email);
            Map<String, Object> kafkaConsumerProperties = getKafkaConsumerProperties(kafkaContainer.getBootstrapServers());
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest("jargrave0", oldPassword))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            try (KafkaConsumer<String, EmailRequest> consumer = new KafkaConsumer<>(kafkaConsumerProperties)) {
                consumer.subscribe(singletonList(topicName));
                mockMvc.perform(post("/auth/password/request/forgotten")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(forgottenPasswordResetRequest)))
                        .andExpect(status().isOk())
                        .andDo(print());

                AtomicReference<EmailRequest> emailRequest = new AtomicReference<>(EmailRequest.builder().build());

                await().atMost(1, SECONDS).until(() -> {
                    ConsumerRecords<String, EmailRequest> records = consumer.poll(ofMillis(100));
                    if (records.isEmpty()) {
                        return false;
                    }
                    assertThat(records.count(), is(1));
                    emailRequest.set(records.iterator().next().value());
                    return true;
                });
                String resetPasswordUrl = (String) emailRequest.get().getData().get("resetPasswordUrl");
                String token = resetPasswordUrl.substring(resetPasswordUrl.indexOf("=") + 1);

                assertThat(resetPasswordUrl, startsWith("http://localhost:4200//password/reset?token="));

                mockMvc.perform(post("/auth/password/reset/forgotten")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ForgottenPasswordResetCommand.builder()
                                        .token(token)
                                        .password(newPassword)
                                        .passwordConfirmation(newPassword)
                                        .build())))
                        .andExpect(status().isOk())
                        .andDo(print());

                mockMvc.perform(post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new LoginRequest("jargrave0", oldPassword))))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                        .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                        .andExpect(jsonPath("$.message").value("BAD_CREDENTIALS"));

                mockMvc.perform(post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new LoginRequest("jargrave0", newPassword))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(1))
                        .andExpect(jsonPath("$.firstName").value("Joletta"))
                        .andExpect(jsonPath("$.lastName").value("Tiger"))
                        .andExpect(jsonPath("$.username").value("jargrave0"))
                        .andExpect(jsonPath("$.email").value(email))
                        .andExpect(jsonPath("$.userRole").value("USER"))
                        .andExpect(jsonPath("$.lastLoginDate").exists())
                        .andExpect(jsonPath("$.joinDate").exists())
                        .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                        .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                        .andExpect(header().exists(AUTHORIZATION))
                        .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            }
        }
    }

    @DisplayName("Should not request forgotten password reset with invalid parameters")
    @Nested
    class ShouldNotRequestForgottenPasswordResetWithInvalidParameters {

        static Stream<Arguments> shouldNotValidateWithBlankEmailArguments() {
            return Stream.of(Arguments.of(of("Should not request forgotten password reset with blank email | (email = \"\")",
                            TestPayload.builder()
                                    .payload(new ForgottenPasswordResetRequest(""))
                                    .build())),
                    Arguments.of(of("Should not request forgotten password reset with blank email | (email = \"\\s\")",
                            TestPayload.builder()
                                    .payload(new ForgottenPasswordResetRequest(" "))
                                    .build())),
                    Arguments.of(of("Should not request forgotten password reset with blank email | (email = \"\\t\")",
                            TestPayload.builder()
                                    .payload(new ForgottenPasswordResetRequest("\t"))
                                    .build())),
                    Arguments.of(of("Should not request forgotten password reset with blank email | (email = \"\\n\")",
                            TestPayload.builder()
                                    .payload(new ForgottenPasswordResetRequest("\n"))
                                    .build())),
                    Arguments.of(of("Should not request forgotten password reset with blank email | (email = \"\\r\")",
                            TestPayload.builder()
                                    .payload(new ForgottenPasswordResetRequest("\r"))
                                    .build())),
                    Arguments.of(of("Should not request forgotten password reset with blank email | (email = \"\\f\")",
                            TestPayload.builder()
                                    .payload(new ForgottenPasswordResetRequest("\f"))
                                    .build())),
                    Arguments.of(of("Should not request forgotten password reset with null email | (email = \"\\null\")",
                            TestPayload.builder()
                                    .payload(new ForgottenPasswordResetRequest(null))
                                    .build())));
        }

        @DisplayName("Should not request forgotten password reset, when email is blank, then should return bad request")
        @MethodSource("shouldNotValidateWithBlankEmailArguments")
        @ParameterizedTest
        void shouldNotRequestForgottenPasswordResetWithBlankEmail(TestPayload<ForgottenPasswordResetRequest> payload) throws Exception {
            // Given
            ForgottenPasswordResetRequest forgottenPasswordResetRequest = payload.payload();
            // When
            String responseJson = mockMvc.perform(post("/auth/password/request/forgotten")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(forgottenPasswordResetRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'EMAIL_NOT_BLANK')]").exists())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'EMAIL_NOT_FOUND')]").exists())
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            assertEquals(2, errors.size());
        }

        @DisplayName("Should not request forgotten password reset, when email does not exist")
        @Test
        void shouldNotRequestForgottenPasswordResetWithNonExistingEmail() throws Exception {
            // Given
            String nonExistingEmail = "test@example.com";
            ForgottenPasswordResetRequest forgottenPasswordResetRequest = new ForgottenPasswordResetRequest(nonExistingEmail);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/password/request/forgotten")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(forgottenPasswordResetRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'EMAIL_NOT_FOUND')]").exists())
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            assertEquals(1, errors.size());
        }
    }

    @DisplayName("Should not reset forgotten password with invalid token")
    @Nested
    class ShouldNotResetForgottenPasswordWithInvalidToken {

        @DisplayName("Should not reset forgotten password, when token is null")
        @Test
        void shouldNotResetForgottenPasswordWithNullTokenCode() throws Exception {
            // Given
            String username = "jargrave0";
            String tokenCode = null;
            String oldPassword = "Password123*";
            String newPassword = "*321drowssaP";
            ForgottenPasswordResetCommand forgottenPasswordResetCommand = ForgottenPasswordResetCommand.builder()
                    .token(tokenCode)
                    .password(newPassword)
                    .passwordConfirmation(newPassword)
                    .build();
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
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
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            mockMvc.perform(post("/auth/password/reset/forgotten")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(forgottenPasswordResetCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'token' && @.message == 'TOKEN_NOT_BLANK')]").exists())
                    .andDo(print());

            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, newPassword))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("BAD_CREDENTIALS"));
        }

        @Test
        void shouldNotResetForgottenPasswordWithBlankToken() throws Exception {
            // Given
            String username = "jargrave0";
            String tokenCode = "";
            String oldPassword = "Password123*";
            String newPassword = "*321drowssaP";
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
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
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            mockMvc.perform(post("/auth/password/reset/forgotten")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ForgottenPasswordResetCommand.builder()
                                    .token(tokenCode)
                                    .password(newPassword)
                                    .passwordConfirmation(newPassword)
                                    .build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'token' && @.message == 'TOKEN_NOT_BLANK')]").exists())
                    .andDo(print());

            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, newPassword))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("BAD_CREDENTIALS"));
        }


        @Test
        void shouldNotResetForgottenPasswordWithTokenWithInvalidToken() throws Exception {
            // Given
            String username = "jargrave0";
            String invalidTokenCode = "invalid-token";
            String oldPassword = "Password123*";
            String newPassword = "*321drowssaP";
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
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
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            mockMvc.perform(post("/auth/password/reset/forgotten")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ForgottenPasswordResetCommand.builder()
                                    .token(invalidTokenCode)
                                    .password(newPassword)
                                    .passwordConfirmation(newPassword)
                                    .build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("PASSWORD_RESET_TOKEN_NOT_FOUND"));

            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, newPassword))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("BAD_CREDENTIALS"));
        }

        @Test
        void shouldNotResetForgottenPasswordWithTokenWithExpiredToken() throws Exception {
            // Given
            Duration expiration = passwordResetTokenConfigurationProperties.expiration();
            Duration exceededExpiration = expiration.plusSeconds(10);
            String username = "jargrave0";
            String tokenCode = "75ecd5ee-4a9e-45d3-9a3d-a77144fe6673";
            String oldPassword = "Password123*";
            String newPassword = "*321drowssaP";
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
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
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));

            when(clock.instant()).thenReturn(Instant.now().plus(exceededExpiration));
            // Then
            mockMvc.perform(post("/auth/password/reset/forgotten")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ForgottenPasswordResetCommand.builder()
                                    .token(tokenCode)
                                    .password(newPassword)
                                    .passwordConfirmation(newPassword)
                                    .build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("PASSWORD_RESET_TOKEN_EXPIRED"));

            assertThat(expiration, lessThan(exceededExpiration));

            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, newPassword))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("BAD_CREDENTIALS"));
        }

        @Test
        void shouldNotResetForgottenPasswordWithAlreadyUsedToken() throws Exception {
            // Given
            String username = "jargrave0";
            String token = "75ecd5ee-4a9e-45d3-9a3d-a77144fe6673";
            String oldPassword = "Password123*";
            String newPassword = "*321drowssaP";
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
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
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));

            mockMvc.perform(post("/auth/password/reset/forgotten")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ForgottenPasswordResetCommand.builder()
                                    .token(token)
                                    .password(newPassword)
                                    .passwordConfirmation(newPassword)
                                    .build())))
                    .andExpect(status().isOk())
                    .andDo(print());

            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, newPassword))))
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
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            mockMvc.perform(post("/auth/password/reset/forgotten")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ForgottenPasswordResetCommand.builder()
                                    .token(token)
                                    .password(newPassword)
                                    .passwordConfirmation(newPassword)
                                    .build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("PASSWORD_RESET_TOKEN_ALREADY_CONFIRMED"))
                    .andDo(print());
        }

    }

    @DisplayName("Should not change forgotten password with invalid new password")
    @Nested
    class ShouldNotChangePasswordWithInvalidPassword {
        @Test
        void shouldNotResetForgottenPasswordWithSamePasswordAsOldOne() throws Exception {
            // Given
            String username = "jargrave0";
            String token = "75ecd5ee-4a9e-45d3-9a3d-a77144fe6673";
            String oldPassword = "Password123*";
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
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
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            mockMvc.perform(post("/auth/password/reset/forgotten")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ForgottenPasswordResetCommand.builder()
                                    .token(token)
                                    .password(oldPassword)
                                    .passwordConfirmation(oldPassword)
                                    .build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_CURRENT_PASSWORD"))
                    .andDo(print());

            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
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
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
        }

        @Test
        void shouldNotResetForgottenPasswordWithTokenWithNullNewPassword() throws Exception {
            // Given
            String username = "jargrave0";
            String token = "75ecd5ee-4a9e-45d3-9a3d-a77144fe6673";
            String oldPassword = "Password123*";
            String newPassword = null;
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
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
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            mockMvc.perform(post("/auth/password/reset/forgotten")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ForgottenPasswordResetCommand.builder()
                                    .token(token)
                                    .password(newPassword)
                                    .passwordConfirmation(newPassword)
                                    .build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'password' && @.message == 'NEW_PASSWORD_NOT_BLANK')]").exists())
                    .andExpect(jsonPath("$.[?(@.field == 'passwordConfirmation' && @.message == 'PASSWORD_CONFIRMATION_NOT_BLANK')]").exists())
                    .andDo(print());

            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
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
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));

            assertNotEquals(oldPassword, newPassword);
        }

        @Test
        void shouldNotResetForgottenPasswordWithNewPasswordConfirmationDifferentFromNewPassword() throws Exception {
            // Given
            String username = "jargrave0";
            String token = "75ecd5ee-4a9e-45d3-9a3d-a77144fe6673";
            String oldPassword = "Password123*";
            String newPassword = "*321drowssaP";
            String newPasswordConfirmation = "*321drowssaP*";
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
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
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            mockMvc.perform(post("/auth/password/reset/forgotten")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ForgottenPasswordResetCommand.builder()
                                    .token(token)
                                    .password(newPassword)
                                    .passwordConfirmation(newPasswordConfirmation)
                                    .build())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'passwordConfirmation' && @.message == 'PASSWORDS_DO_NOT_MATCH')]").exists())
                    .andDo(print());

            assertNotEquals(oldPassword, newPassword);
            assertNotEquals(newPassword, newPasswordConfirmation);
        }
    }

    @DisplayName("Should not reset forgotten password with invalid password pattern")
    @Nested
    class ShouldNotResetForgottenPasswordWithInvalidPasswordPattern {

        static Stream<Arguments> shouldNotResetForgottenPasswordWithBInvalidPasswordPatternArguments() {
            return Stream.of(Arguments.of(of("Should not reset forgotten password reset with invalid password pattern | (pattern = shorter than 8 characters)",
                            TestPayload.builder()
                                    .payload(ForgottenPasswordResetCommand.builder()
                                            .token("75ecd5ee-4a9e-45d3-9a3d-a77144fe6673")
                                            .password("Passw1*")
                                            .passwordConfirmation("Passw1*")
                                            .build())
                                    .expectedMessage("PASSWORD_MUST_BE_8_OR_MORE_CHARACTERS_IN_LENGTH")
                                    .build())),
                    Arguments.of(of("Should not reset forgotten password reset with invalid password pattern | (pattern = longer than 16 characters)",
                            TestPayload.builder()
                                    .payload(
                                            ForgottenPasswordResetCommand.builder()
                                                    .token("75ecd5ee-4a9e-45d3-9a3d-a77144fe6673")
                                                    .password("Password123456789*")
                                                    .passwordConfirmation("Password123456789*")
                                                    .build())
                                    .expectedMessage("PASSWORD_MUST_BE_NO_MORE_THAN_16_CHARACTERS_IN_LENGTH")
                                    .build())),
                    Arguments.of(of("Should not reset forgotten password reset with invalid password pattern | (pattern = whitespace characters)",
                            TestPayload.builder()
                                    .payload(
                                            ForgottenPasswordResetCommand.builder()
                                                    .token("75ecd5ee-4a9e-45d3-9a3d-a77144fe6673")
                                                    .password("Password123* ")
                                                    .passwordConfirmation("Password123* ")
                                                    .build())
                                    .expectedMessage("PASSWORD_CONTAINS_A_WHITESPACE_CHARACTER")
                                    .build())),
                    Arguments.of(of("Should not reset forgotten password reset with invalid password pattern | (pattern = no uppercase)",
                            TestPayload.builder()
                                    .payload(
                                            ForgottenPasswordResetCommand.builder()
                                                    .token("75ecd5ee-4a9e-45d3-9a3d-a77144fe6673")
                                                    .password("password123*")
                                                    .passwordConfirmation("password123*")
                                                    .build())
                                    .expectedMessage("PASSWORD_MUST_CONTAIN_1_OR_MORE_UPPERCASE_CHARACTERS")
                                    .build())),
                    Arguments.of(of("Should not reset forgotten password reset with invalid password pattern | (pattern = no lowercase)",
                            TestPayload.builder()
                                    .payload(
                                            ForgottenPasswordResetCommand.builder()
                                                    .token("75ecd5ee-4a9e-45d3-9a3d-a77144fe6673")
                                                    .password("PASSWORD123*")
                                                    .passwordConfirmation("PASSWORD123*")
                                                    .build())
                                    .expectedMessage("PASSWORD_MUST_CONTAIN_1_OR_MORE_LOWERCASE_CHARACTERS")
                                    .build())),
                    Arguments.of(of("Should not reset forgotten password reset with invalid password pattern | (pattern = no digit)",
                            TestPayload.builder()
                                    .payload(
                                            ForgottenPasswordResetCommand.builder()
                                                    .token("75ecd5ee-4a9e-45d3-9a3d-a77144fe6673")
                                                    .password("Password*")
                                                    .passwordConfirmation("Password*")
                                                    .build())
                                    .expectedMessage("PASSWORD_MUST_CONTAIN_1_OR_MORE_DIGIT_CHARACTERS")
                                    .build())),
                    Arguments.of(of("Should not reset forgotten password reset with invalid password pattern | (pattern = no special character)",
                            TestPayload.builder()
                                    .payload(
                                            ForgottenPasswordResetCommand.builder()
                                                    .token("75ecd5ee-4a9e-45d3-9a3d-a77144fe6673")
                                                    .password("Password123")
                                                    .passwordConfirmation("Password123")
                                                    .build())
                                    .expectedMessage("PASSWORD_MUST_CONTAIN_1_OR_MORE_SPECIAL_CHARACTERS")
                                    .build())));
        }

        @MethodSource("shouldNotResetForgottenPasswordWithBInvalidPasswordPatternArguments")
        @ParameterizedTest
        void shouldNotResetForgottenPasswordWithInvalidPasswordPattern(TestPayload<ForgottenPasswordResetCommand> payload) throws Exception {
            // Given
            String username = "jargrave0";
            String oldPassword = "Password123*";
            ForgottenPasswordResetCommand forgottenPasswordResetCommand = payload.payload();
            String expectedMessage = payload.expectedMessage();
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
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
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            mockMvc.perform(post("/auth/password/reset/forgotten")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(forgottenPasswordResetCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'password' && @.message == '" + expectedMessage + "')]").exists())
                    .andDo(print());
        }
    }

    @DisplayName("Should change password")
    @Nested
    class ShouldResetPassword {
        @Test
        void shouldResetPasswordAsUser() throws Exception {
            // Given
            String oldPassword = "Password123*";
            String newPassword = "*123drowssaP";
            String newPasswordConfirmation = "*123drowssaP";
            String username = "jargrave0";
            long userId = 1L;
            PasswordResetCommand passwordResetCommand = PasswordResetCommand.builder()
                    .userId(userId)
                    .currentPassword(oldPassword)
                    .newPassword(newPassword)
                    .confirmNewPassword(newPasswordConfirmation)
                    .build();
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            mockMvc.perform(post("/auth/password/reset")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(passwordResetCommand))
                            .header(AUTHORIZATION, getAuthorizationHeader(username)))
                    .andExpect(status().isOk())
                    .andDo(print());

            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, newPassword))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
        }

        @Test
        void shouldChangePasswordAsAdmin() throws Exception {
            // Given
            String oldPassword = "Password123*";
            String newPassword = "*123drowssaP";
            String newPasswordConfirmation = "*123drowssaP";
            String username = "jargrave0";
            long userId = 1L;
            PasswordResetCommand passwordResetCommand = PasswordResetCommand.builder()
                    .userId(userId)
                    .currentPassword(oldPassword)
                    .newPassword(newPassword)
                    .confirmNewPassword(newPasswordConfirmation)
                    .build();
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            mockMvc.perform(post("/auth/password/reset")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(passwordResetCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andDo(print());

            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, newPassword))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
        }

        private String getAuthorizationHeader(String username) throws Exception {
            return mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, "Password123*"))))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")))
                    .andReturn()
                    .getResponse()
                    .getHeader(AUTHORIZATION);
        }

        private String getAdminAuthorizationHeader() throws Exception {
            return mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest("celders1", "Password123*"))))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")))
                    .andReturn()
                    .getResponse()
                    .getHeader(AUTHORIZATION);
        }
    }

    @DisplayName("Should not change password with invalid parameters")
    @Nested
    class ChangePasswordWithInvalidParameters {


    }

    @DisplayName("Should not reset password")
    @Nested
    class ShouldNotResetPassword {

        @Test
        void shouldNotChangePasswordAsNotAuthorizedUser() throws Exception {
            // Given
            String oldPassword = "Password123*";
            String newPassword = "*123drowssaP";
            String newPasswordConfirmation = "*123drowssaP";
            String username = "jargrave0";
            String notAuthorizedUsername = "bsabathier2";
            long userId = 1L;
            PasswordResetCommand passwordResetCommand = PasswordResetCommand.builder()
                    .userId(userId)
                    .currentPassword(oldPassword)
                    .newPassword(newPassword)
                    .confirmNewPassword(newPasswordConfirmation)
                    .build();
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            String responseJson = mockMvc.perform(post("/auth/password/reset")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(passwordResetCommand))
                            .header(AUTHORIZATION, getAuthorizationHeader(notAuthorizedUsername)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.httpStatusCode").value(UNAUTHORIZED.value()))
                    .andExpect(jsonPath("$.httpStatus").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value(ACCESS_DENIED_MESSAGE))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            HttpResponse response = objectMapper.readValue(responseJson, HttpResponse.class);
            assertThat(response.timestamp(), within(1, MINUTES, now()));
            assertNotEquals(username, notAuthorizedUsername);
        }

        private String getAuthorizationHeader(String username) throws Exception {
            return mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, "Password123*"))))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")))
                    .andReturn()
                    .getResponse()
                    .getHeader(AUTHORIZATION);
        }

        @Test
        void shouldNotChangePasswordWithNewPasswordSameAsCurrentOne() throws Exception {
            // Given
            String oldPassword = "Password123*";
            String newPassword = "Password123*";
            String newPasswordConfirmation = "Password123*";
            String username = "jargrave0";
            long userId = 1L;
            PasswordResetCommand passwordResetCommand = PasswordResetCommand.builder()
                    .userId(userId)
                    .currentPassword(oldPassword)
                    .newPassword(newPassword)
                    .confirmNewPassword(newPasswordConfirmation)
                    .build();
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, oldPassword))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            mockMvc.perform(post("/auth/password/reset")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(passwordResetCommand))
                            .header(AUTHORIZATION, getAuthorizationHeader(username)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_CURRENT_PASSWORD"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
        }

        @Test
        void shouldNotChangePasswordWithInvalidOldPassword() throws Exception {
            // Given
            String validOldPassword = "Password123*";
            String invalidOldPassword = "Password123";
            String newPassword = "Password123*";
            String newPasswordConfirmation = "Password123*";
            String username = "jargrave0";
            long userId = 1L;
            PasswordResetCommand passwordResetCommand = PasswordResetCommand.builder()
                    .userId(userId)
                    .currentPassword(invalidOldPassword)
                    .newPassword(newPassword)
                    .confirmNewPassword(newPasswordConfirmation)
                    .build();
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, validOldPassword))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(header().exists(AUTHORIZATION))
                    .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            // Then
            String responseJson = mockMvc.perform(post("/auth/password/reset")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(passwordResetCommand))
                            .header(AUTHORIZATION, getAuthorizationHeader(username)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("INVALID_CURRENT_PASSWORD"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            HttpResponse response = objectMapper.readValue(responseJson, HttpResponse.class);
            assertThat(response.timestamp(), within(1, MINUTES, now()));
            assertNotEquals(username, invalidOldPassword);
        }
    }
}