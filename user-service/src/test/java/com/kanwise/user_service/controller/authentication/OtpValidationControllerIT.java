package com.kanwise.user_service.controller.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.user_service.configuration.security.otp.OtpConfigurationProperties;
import com.kanwise.user_service.model.authentication.request.LoginRequest;
import com.kanwise.user_service.model.error.ValidationErrorDto;
import com.kanwise.user_service.model.notification.email.EmailRequest;
import com.kanwise.user_service.model.otp.OtpValidationRequest;
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

import static com.kanwise.user_service.controller.kafka.KafkaTestingUtils.getKafkaConsumerProperties;
import static java.time.Duration.ofMillis;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.clients.admin.AdminClient.create;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
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
@AutoConfigureMockMvc
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@Testcontainers
class OtpValidationControllerIT {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(parse("confluentinc/cp-kafka:latest"));
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DatabaseCleaner databaseCleaner;
    private final OtpConfigurationProperties otpConfigurationProperties;
    private final AdminClient kafkaAdminClient;
    @MockBean
    private Clock clock;

    @Autowired
    public OtpValidationControllerIT(MockMvc mockMvc, ObjectMapper objectMapper, DatabaseCleaner databaseCleaner, OtpConfigurationProperties otpConfigurationProperties, KafkaAdmin kafkaAdmin) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.databaseCleaner = databaseCleaner;
        this.otpConfigurationProperties = otpConfigurationProperties;
        this.kafkaAdminClient = create(kafkaAdmin.getConfigurationProperties());
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

    @DisplayName("Should now validate registration otp with invalid parameters")
    @Nested
    class ShouldNotValidateRegistrationOtpWithInvalidParameters {

        static Stream<Arguments> shouldNotValidateWithBlankCodeArguments() {
            return Stream.of(Arguments.of(of("Should validate otp with blank code | (code = \"\")",
                            TestPayload.builder()
                                    .payload(new OtpValidationRequest(1L, ""))
                                    .build())),
                    Arguments.of(of("Should validate otp with blank code | (code = \"\\s\")",
                            TestPayload.builder()
                                    .payload(new OtpValidationRequest(1L, " "))
                                    .build())),
                    Arguments.of(of("Should validate otp with blank code | (code = \"\\t\")",
                            TestPayload.builder()
                                    .payload(new OtpValidationRequest(1L, "\t"))
                                    .build())),
                    Arguments.of(of("Should validate otp with blank code | (code = \"\\n\")",
                            TestPayload.builder()
                                    .payload(new OtpValidationRequest(1L, "\n"))
                                    .build())),
                    Arguments.of(of("Should validate otp with blank code | (code = \"\\r\")",
                            TestPayload.builder()
                                    .payload(new OtpValidationRequest(1L, "\r"))
                                    .build())),
                    Arguments.of(of("Should validate otp with blank code | (code = \"\\f\")",
                            TestPayload.builder()
                                    .payload(new OtpValidationRequest(1L, "\f"))
                                    .build())));
        }

        @Test
        void shouldNotValidateWithNullOtpId() throws Exception {
            // Given
            OtpValidationRequest otpValidationRequest = new OtpValidationRequest(null, "123456");
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/registration/otp/sms")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(otpValidationRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[0].field").value("otpId"))
                    .andExpect(jsonPath("$.[?(@.field == 'otpId' && @.message == 'OTP_NOT_FOUND')]").exists())
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> response = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            assertEquals(1, response.size());
        }

        @MethodSource("shouldNotValidateWithBlankCodeArguments")
        @ParameterizedTest
        void shouldNotValidateWithBlankCode(TestPayload<OtpValidationRequest> payload) throws Exception {
            // Given
            OtpValidationRequest otpValidationRequest = payload.payload();
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/registration/otp/sms")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(otpValidationRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'code' && @.message == 'CODE_NOT_BLANK')]").exists())
                    .andExpect(jsonPath("$.[?(@.field == 'code' && @.message == 'CODE_MUST_BE_6_DIGITS_LONG')]").exists())
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            assertEquals(2, errors.size());
        }

        @DisplayName("Should not validate registration otp with too long code")
        @Test
        void shouldNotValidateWithTooLongCode() throws Exception {
            // Given
            String tooLongCode = "1234567";
            long requiredCodeLength = otpConfigurationProperties.length();
            OtpValidationRequest otpValidationRequest = new OtpValidationRequest(1L, tooLongCode);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/registration/otp/sms")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(otpValidationRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'code' && @.message == 'CODE_MUST_BE_6_DIGITS_LONG')]").exists())
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            assertEquals(1, errors.size());
            assertThat((long) tooLongCode.length(), greaterThan(requiredCodeLength));
        }

        @DisplayName("Should not validate registration otp with too short code")
        @Test
        void shouldNotValidateWithTooShortCode() throws Exception {
            // Given
            String tooShortCode = "12345";
            long requiredCodeLength = otpConfigurationProperties.length();
            OtpValidationRequest otpValidationRequest = new OtpValidationRequest(1L, tooShortCode);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/registration/otp/sms")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(otpValidationRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'code' && @.message == 'CODE_MUST_BE_6_DIGITS_LONG')]").exists())
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            assertEquals(1, errors.size());
            assertThat((long) tooShortCode.length(), lessThan(requiredCodeLength));
        }
    }

    @DisplayName("Should validate registration otp")
    @Nested
    class ShouldValidateRegistrationOtp {
        @DisplayName("Should validate registration otp, if otp is valid, new password email request should be produced and account should be activated")
        @Test
        void shouldValidateRegistrationOtp() throws Exception {
            // Given
            OtpValidationRequest otpValidationRequest = new OtpValidationRequest(1L, "123456");
            String username = "abeaston1d";
            String topicName = "notification-email";
            Map<String, Object> kafkaConsumerProperties = getKafkaConsumerProperties(kafkaContainer.getBootstrapServers());
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
            try (KafkaConsumer<String, EmailRequest> consumer = new KafkaConsumer<>(kafkaConsumerProperties)) {
                consumer.subscribe(singletonList(topicName));
                mockMvc.perform(post("/auth/registration/otp/sms")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(otpValidationRequest)))
                        .andExpect(status().isOk());

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

                String password = (String) emailRequest.get().getData().get("password");
                LoginRequest loginRequest = new LoginRequest(username, password);

                mockMvc.perform(post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(50))
                        .andExpect(jsonPath("$.firstName").value("Alidia"))
                        .andExpect(jsonPath("$.lastName").value("Beaston"))
                        .andExpect(jsonPath("$.username").value(username))
                        .andExpect(jsonPath("$.email").value("alidiabeaston.kanwise@gmail.com"))
                        .andExpect(jsonPath("$.userRole").value("USER"))
                        .andExpect(jsonPath("$.lastLoginDate").exists())
                        .andExpect(jsonPath("$.joinDate").exists())
                        .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                        .andExpect(jsonPath("$.phoneNumber").value("+46 699 491 9032"))
                        .andExpect(header().exists(AUTHORIZATION))
                        .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            }
        }
    }

    @Nested
    class ShouldNotValidateRegistrationOtp {
        @DisplayName("Should not validate registration otp, if otp is expired")
        @Test
        void shouldNotValidateRegistrationOtpIfOtpIsExpired() throws Exception {
            // Given
            OtpValidationRequest otpValidationRequest = new OtpValidationRequest(1L, "123456");
            String username = "abeaston1d";
            Duration otpExpirationDuration = otpConfigurationProperties.expiration();
            Duration exceededOtpExpirationDuration = Duration.ofMinutes(10);
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, "password"))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("USER_IS_DISABLED"));

            when(clock.instant()).thenReturn(Instant.now().plus(exceededOtpExpirationDuration));
            // Then
            String responseJson = mockMvc.perform(post("/auth/registration/otp/sms")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(otpValidationRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("OTP_HAS_EXPIRED"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, "password"))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("USER_IS_DISABLED"));

            HttpResponse response = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            assertThat(response.timestamp(), within(1, MINUTES, now()));
            assertThat(otpExpirationDuration, lessThan(exceededOtpExpirationDuration));
        }

        @DisplayName("Should not validate registration otp, if otp code is invalid")
        @Test
        void shouldNotValidateRegistrationOtpIfOtpCodeIsInvalid() throws Exception {
            // Given
            String validCode = "123456";
            String invalidCode = "654321";
            OtpValidationRequest otpValidationRequest = new OtpValidationRequest(1L, invalidCode);
            String username = "abeaston1d";
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
            String responseJson = mockMvc.perform(post("/auth/registration/otp/sms")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(otpValidationRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("OTP_INVALID_CODE"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            HttpResponse response = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            assertThat(response.timestamp(), within(1, MINUTES, now()));
            assertNotEquals(validCode, invalidCode);

            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, "password"))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("USER_IS_DISABLED"));
        }

        @DisplayName("Should not validate registration otp, if otp code is already confirmed")
        @Test
        void shouldNotValidateRegistrationOtpIfOtpIsAlreadyConfirmed() throws Exception {
            // Given
            OtpValidationRequest otpValidationRequest = new OtpValidationRequest(1L, "123456");
            String username = "abeaston1d";
            String topicName = "notification-email";
            Map<String, Object> kafkaConsumerProperties = getKafkaConsumerProperties(kafkaContainer.getBootstrapServers());
            // When
            mockMvc.perform(post("/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, "password"))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("USER_IS_DISABLED"));

            try (KafkaConsumer<String, EmailRequest> consumer = new KafkaConsumer<>(kafkaConsumerProperties)) {
                consumer.subscribe(singletonList(topicName));
                mockMvc.perform(post("/auth/registration/otp/sms")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(otpValidationRequest)))
                        .andExpect(status().isOk());

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

                String password = (String) emailRequest.get().getData().get("password");
                LoginRequest loginRequest = new LoginRequest(username, password);

                mockMvc.perform(post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(50))
                        .andExpect(jsonPath("$.firstName").value("Alidia"))
                        .andExpect(jsonPath("$.lastName").value("Beaston"))
                        .andExpect(jsonPath("$.username").value(username))
                        .andExpect(jsonPath("$.email").value("alidiabeaston.kanwise@gmail.com"))
                        .andExpect(jsonPath("$.userRole").value("USER"))
                        .andExpect(jsonPath("$.lastLoginDate").exists())
                        .andExpect(jsonPath("$.joinDate").exists())
                        .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                        .andExpect(jsonPath("$.phoneNumber").value("+46 699 491 9032"))
                        .andExpect(header().exists(AUTHORIZATION))
                        .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            }
            // Then
            String responseJson = mockMvc.perform(post("/auth/registration/otp/sms")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(otpValidationRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("OTP_ALREADY_CONFIRMED"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            HttpResponse response = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            assertThat(response.timestamp(), within(1, MINUTES, now()));
        }

        @DisplayName("Should not validate registration otp, if otp has not been delivered")
        @Test
        void shouldNotValidateRegistrationOtpIfOtpHasNotBeenDelivered() throws Exception {
            // Given
            OtpValidationRequest otpValidationRequest = new OtpValidationRequest(2L, "123456");
            String username = "abeaston1d";
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
            String responseJson = mockMvc.perform(post("/auth/registration/otp/sms")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(otpValidationRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("OTP_NOT_DELIVERED"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            HttpResponse response = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            assertThat(response.timestamp(), within(1, MINUTES, now()));
        }
    }
}