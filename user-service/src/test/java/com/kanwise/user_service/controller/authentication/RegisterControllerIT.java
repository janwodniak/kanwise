package com.kanwise.user_service.controller.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.clients.kanwise_service.member.MemberClient;
import com.kanwise.clients.report_service.subscriber.client.SubscriberClient;
import com.kanwise.user_service.model.authentication.request.LoginRequest;
import com.kanwise.user_service.model.authentication.request.RegisterRequest;
import com.kanwise.user_service.model.authentication.response.dto.RegisterResponseDto;
import com.kanwise.user_service.model.error.ValidationErrorDto;
import com.kanwise.user_service.model.notification.email.EmailRequest;
import com.kanwise.user_service.model.notification.sms.OtpSmsRequest;
import com.kanwise.user_service.test.DatabaseCleaner;
import liquibase.exception.LiquibaseException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.kanwise.user_service.controller.kafka.KafkaTestingUtils.getKafkaConsumerProperties;
import static java.time.Duration.ofMillis;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.clients.admin.AdminClient.create;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.kafka.config.TopicBuilder.name;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static org.testcontainers.utility.DockerImageName.parse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class RegisterControllerIT {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(parse("confluentinc/cp-kafka:latest"));
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DatabaseCleaner databaseCleaner;
    private final AdminClient kafkaAdminClient;
    @MockBean
    private MemberClient memberClient;
    @MockBean
    private SubscriberClient subscriberClient;

    @Autowired
    public RegisterControllerIT(MockMvc mockMvc, ObjectMapper objectMapper, DatabaseCleaner databaseCleaner, MemberClient memberClient, KafkaAdmin kafkaAdmin) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.databaseCleaner = databaseCleaner;
        this.memberClient = memberClient;
        this.kafkaAdminClient = create(kafkaAdmin.getConfigurationProperties());
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        Mockito.when(memberClient.addMember(Mockito.any())).thenReturn(ResponseEntity.ok().body(null));
        Mockito.when(subscriberClient.addSubscriber(Mockito.any())).thenReturn(ResponseEntity.ok().body(null));
        kafkaAdminClient.createTopics(List.of(name("notification-sms").build(), name("notification-email").build()));
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        kafkaAdminClient.deleteTopics(List.of("notification-sms", "notification-email"));
        kafkaAdminClient.close();
        databaseCleaner.cleanUp();
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

    @Nested
    @DisplayName("Should register")
    class ShouldRegister {
        @Test
        void shouldRegisterWithDisabledTwoFactorAuthentication() throws Exception {
            // Given
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("Johnny")
                    .lastName("Bean")
                    .username("redbean")
                    .email("bean.kanwise@gmail.com")
                    .phoneNumber(null)
                    .twoFactorEnabled(false)
                    .build();
            Map<String, Object> kafkaConsumerProperties = getKafkaConsumerProperties(kafkaContainer.getBootstrapServers());
            String topicName = "notification-email";
            // When
            // Then
            try (KafkaConsumer<String, EmailRequest> consumer = new KafkaConsumer<>(kafkaConsumerProperties)) {
                consumer.subscribe(singletonList(topicName));
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.user.id").value(51))
                        .andExpect(jsonPath("$.user.firstName").value(registerRequest.firstName()))
                        .andExpect(jsonPath("$.user.lastName").value(registerRequest.lastName()))
                        .andExpect(jsonPath("$.user.username").value(registerRequest.username()))
                        .andExpect(jsonPath("$.user.email").value(registerRequest.email()))
                        .andExpect(jsonPath("$.user.userRole").value("USER"))
                        .andExpect(jsonPath("$.user.lastLoginDate").exists())
                        .andExpect(jsonPath("$.user.joinDate").exists())
                        .andExpect(jsonPath("$.user.twoFactorEnabled").value(false))
                        .andExpect(jsonPath("$.user.phoneNumber").doesNotExist())
                        .andExpect(jsonPath("$.otpId").doesNotExist())
                        .andExpect(jsonPath("$.profileImageUrl").doesNotExist());

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
                LoginRequest loginRequest = new LoginRequest(registerRequest.username(), password);
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(51))
                        .andExpect(jsonPath("$.firstName").value(registerRequest.firstName()))
                        .andExpect(jsonPath("$.lastName").value(registerRequest.lastName()))
                        .andExpect(jsonPath("$.username").value(registerRequest.username()))
                        .andExpect(jsonPath("$.email").value(registerRequest.email()))
                        .andExpect(jsonPath("$.userRole").value("USER"))
                        .andExpect(jsonPath("$.lastLoginDate").exists())
                        .andExpect(jsonPath("$.joinDate").exists())
                        .andExpect(jsonPath("$.twoFactorEnabled").value(false))
                        .andExpect(jsonPath("$.phoneNumber").doesNotExist())
                        .andExpect(jsonPath("$.profileImageUrl").doesNotExist())
                        .andExpect(header().exists(AUTHORIZATION))
                        .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            }
        }

        @Test
        void shouldRegisterWithEnabledTwoFactorAuthentication() throws Exception {
            // Given
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("Johnny")
                    .lastName("Bean")
                    .username("redbean")
                    .email("bean.kanwisetest@gmail.com")
                    .phoneNumber("+48123456789")
                    .twoFactorEnabled(true)
                    .build();
            Map<String, Object> kafkaConsumerProperties = getKafkaConsumerProperties(kafkaContainer.getBootstrapServers());
            String topicName = "notification-sms";
            // When
            // Then
            try (KafkaConsumer<String, OtpSmsRequest> consumer = new KafkaConsumer<>(kafkaConsumerProperties)) {
                consumer.subscribe(singletonList(topicName));
                String responseJson = mockMvc.perform(post("/auth/register")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                        .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.user.id").value(51))
                        .andExpect(jsonPath("$.user.firstName").value(registerRequest.firstName()))
                        .andExpect(jsonPath("$.user.lastName").value(registerRequest.lastName()))
                        .andExpect(jsonPath("$.user.username").value(registerRequest.username()))
                        .andExpect(jsonPath("$.user.email").value(registerRequest.email()))
                        .andExpect(jsonPath("$.user.userRole").value("USER"))
                        .andExpect(jsonPath("$.user.lastLoginDate").exists())
                        .andExpect(jsonPath("$.user.joinDate").exists())
                        .andExpect(jsonPath("$.user.twoFactorEnabled").value(true))
                        .andExpect(jsonPath("$.user.phoneNumber").value(registerRequest.phoneNumber()))
                        .andExpect(jsonPath("$.otpId").exists())
                        .andExpect(jsonPath("$.profileImageUrl").doesNotExist())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                RegisterResponseDto registerResponse = objectMapper.readValue(responseJson, RegisterResponseDto.class);
                AtomicReference<OtpSmsRequest> otpSmsRequest = new AtomicReference<>(OtpSmsRequest.builder().build());

                await().atMost(1, SECONDS).until(() -> {
                    ConsumerRecords<String, OtpSmsRequest> records = consumer.poll(ofMillis(100));
                    if (records.isEmpty()) {
                        return false;
                    }
                    assertThat(records.count(), is(1));
                    otpSmsRequest.set(records.iterator().next().value());
                    return true;
                });

                registerResponse.otpId().ifPresentOrElse(
                        otpId -> Assertions.assertEquals(otpId, otpSmsRequest.get().getOtpId()),
                        Assertions::fail
                );
            }
        }
    }

    @DisplayName("Should not register")
    @Nested
    class ShouldNotRegister {

        @Test
        void shouldNotRegisterWithNullEmail() throws Exception {
            // Given
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("Johnny")
                    .lastName("Bean")
                    .username("redbean")
                    .email(null)
                    .phoneNumber(null)
                    .twoFactorEnabled(false)
                    .build();
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'EMAIL_NOT_NULL')]").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            Assertions.assertEquals(1, errors.size());
        }

        @Test
        void shouldNotRegisterWithInvalidEmailPattern() throws Exception {
            // Given
            String invalidEmail = "kanwisetest";
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("Johnny")
                    .lastName("Bean")
                    .username("redbean")
                    .email(invalidEmail)
                    .phoneNumber(null)
                    .twoFactorEnabled(false)
                    .build();
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'INVALID_EMAIL_PATTERN')]").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            Assertions.assertEquals(1, errors.size());
        }

        @Test
        void shouldNotRegisterWithNotUniqueEmail() throws Exception {
            // Given
            String notUniqueEmail = "jolettatiger.kanwise@gmail.com";
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("Johnny")
                    .lastName("Bean")
                    .username("redbean")
                    .email(notUniqueEmail)
                    .phoneNumber(null)
                    .twoFactorEnabled(false)
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(notUniqueEmail));
            // Then
            String responseJson = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'EMAIL_NOT_UNIQUE')]").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            Assertions.assertEquals(1, errors.size());
        }

        @Test
        void shouldNotRegisterWithNotUniqueUsername() throws Exception {
            // Given
            String notUniqueUsername = "jargrave0";
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .firstName("Johnny")
                    .lastName("Bean")
                    .username(notUniqueUsername)
                    .email("testuser.kanwise@gmail.com")
                    .phoneNumber(null)
                    .twoFactorEnabled(false)
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.username").value(notUniqueUsername));
            // Then
            String responseJson = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_UNIQUE')]").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            Assertions.assertEquals(1, errors.size());
        }

        @Test
        void shouldNotRegisterWithNullFirstName() throws Exception {
            // Given
            RegisterRequest registerRequest = new RegisterRequest(null, "Bean", "redbean", "kanwisetest@gmail.com", null, false);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'firstName' && @.message == 'FIRST_NAME_NOT_NULL')]").exists())
                    .andExpect(jsonPath("$.[?(@.field == 'firstName' && @.message == 'FIRST_NAME_NOT_BLANK')]").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            Assertions.assertEquals(2, errors.size());
        }

        @Test
        void shouldNotRegisterWithBlankFirstName() throws Exception {
            // Given
            RegisterRequest registerRequest = new RegisterRequest("", "Bean", "redbean", "kanwisetest@gmail.com", null, false);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'firstName' && @.message == 'FIRST_NAME_NOT_BLANK')]").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            Assertions.assertEquals(1, errors.size());
        }

        @Test
        void shouldNotRegisterWithNullLastName() throws Exception {
            // Given
            RegisterRequest registerRequest = new RegisterRequest("Johnny", null, "redbean", "kanwisetest@gmail.com", null, false);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'lastName' && @.message == 'LAST_NAME_NOT_NULL')]").exists())
                    .andExpect(jsonPath("$.[?(@.field == 'lastName' && @.message == 'LAST_NAME_NOT_BLANK')]").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            Assertions.assertEquals(2, errors.size());
        }

        @Test
        void shouldNotRegisterWithBlankLastName() throws Exception {
            // Given
            RegisterRequest registerRequest = new RegisterRequest("Johnny", "", "redbean", "kanwisetest@gmail.com", null, false);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'lastName' && @.message == 'LAST_NAME_NOT_BLANK')]").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            Assertions.assertEquals(1, errors.size());
        }

        @Test
        void shouldNotRegisterWithNullUsername() throws Exception {
            // Given
            RegisterRequest registerRequest = new RegisterRequest("Johnny", "Bean", null, "kanwisetest@gmail.com", null, false);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_NULL')]").exists())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_BLANK')]").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            Assertions.assertEquals(2, errors.size());
        }

        @Test
        void shouldNotRegisterWithBlankUsername() throws Exception {
            // Given
            RegisterRequest registerRequest = new RegisterRequest("Johnny", "Bean", "", "kanwise@gmail.com", null, false);
            // When
            // Then
            String responseJson = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_BLANK')]").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
            });
            Assertions.assertEquals(1, errors.size());
        }
    }

}