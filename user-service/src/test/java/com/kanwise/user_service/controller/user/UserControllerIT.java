package com.kanwise.user_service.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.clients.kanwise_service.member.MemberClient;
import com.kanwise.clients.report_service.subscriber.client.SubscriberClient;
import com.kanwise.user_service.model.authentication.request.LoginRequest;
import com.kanwise.user_service.model.notification.email.EmailMessageType;
import com.kanwise.user_service.model.notification.email.EmailRequest;
import com.kanwise.user_service.model.user.command.CreateUserCommand;
import com.kanwise.user_service.model.user.command.EditUserCommand;
import com.kanwise.user_service.model.user.command.EditUserPartiallyCommand;
import com.kanwise.user_service.test.DatabaseCleaner;
import liquibase.exception.LiquibaseException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static com.kanwise.user_service.model.authentication.two_factor_authentication.TwoFactorAction.CHANGE_PASSWORD;
import static com.kanwise.user_service.model.authentication.two_factor_authentication.TwoFactorAction.LOGIN;
import static com.kanwise.user_service.model.authentication.two_factor_authentication.TwoFactorAction.RESET_PASSWORD;
import static com.kanwise.user_service.model.notification.subscribtions.UserNotificationType.PASSWORD_RESET;
import static com.kanwise.user_service.model.notification.subscribtions.UserNotificationType.PASSWORD_UPDATED;
import static com.kanwise.user_service.model.notification.subscribtions.UserNotificationType.USER_BLOCKED;
import static com.kanwise.user_service.model.notification.subscribtions.UserNotificationType.USER_DELETED;
import static com.kanwise.user_service.model.notification.subscribtions.UserNotificationType.USER_UNBLOCKED;
import static com.kanwise.user_service.model.notification.subscribtions.UserNotificationType.USER_UPDATED;
import static java.time.Duration.ofMillis;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.clients.admin.AdminClient.create;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.kafka.config.TopicBuilder.name;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class UserControllerIT {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(parse("confluentinc/cp-kafka:latest"));
    private final MockMvc mockMvc;
    private final DatabaseCleaner databaseCleaner;
    private final ObjectMapper objectMapper;
    private final AdminClient kafkaAdminClient;

    @MockBean
    private MemberClient memberClient;
    @MockBean
    private SubscriberClient subscriberClient;

    @Autowired
    public UserControllerIT(MockMvc mockMvc, DatabaseCleaner databaseCleaner, ObjectMapper objectMapper, KafkaAdmin kafkaAdmin) {
        this.mockMvc = mockMvc;
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
        when(memberClient.addMember(any())).thenReturn(new ResponseEntity<>(CREATED));
        when(subscriberClient.addSubscriber(any())).thenReturn(new ResponseEntity<>(CREATED));
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
    class ShouldCreateUser {
        @Test
        void shouldCreateUser() throws Exception {
            // Given
            CreateUserCommand createUserCommand = CreateUserCommand.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .username("johndoe")
                    .email("johndoe.kanwise@gmail.com")
                    .build();
            Map<String, Object> kafkaConsumerProperties = getKafkaConsumerProperties(kafkaContainer.getBootstrapServers());
            String topicName = "notification-email";
            // When
            // Then
            try (KafkaConsumer<String, EmailRequest> consumer = new KafkaConsumer<>(kafkaConsumerProperties)) {
                consumer.subscribe(singletonList(topicName));
                mockMvc.perform(post("/user")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createUserCommand))
                                .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").value(51))
                        .andExpect(jsonPath("$.firstName").value(createUserCommand.firstName()))
                        .andExpect(jsonPath("$.lastName").value(createUserCommand.lastName()))
                        .andExpect(jsonPath("$.username").value(createUserCommand.username()))
                        .andExpect(jsonPath("$.userRole").value("USER"))
                        .andExpect(jsonPath("$.joinDate").exists())
                        .andExpect(jsonPath("$.twoFactorEnabled").value(false));

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

                assertThat(emailRequest.get().getTo(), is(createUserCommand.email()));
                assertThat(emailRequest.get().getType(), is(EmailMessageType.ACCOUNT_CREATED));
                String password = (String) emailRequest.get().getData().get("password");

                mockMvc.perform(post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                        .username(createUserCommand.username())
                                        .password(password)
                                        .build())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(51))
                        .andExpect(jsonPath("$.firstName").value(createUserCommand.firstName()))
                        .andExpect(jsonPath("$.lastName").value(createUserCommand.lastName()))
                        .andExpect(jsonPath("$.username").value(createUserCommand.username()))
                        .andExpect(jsonPath("$.email").value(createUserCommand.email()))
                        .andExpect(jsonPath("$.userRole").value("USER"))
                        .andExpect(jsonPath("$.lastLoginDate").exists())
                        .andExpect(jsonPath("$.joinDate").exists())
                        .andExpect(jsonPath("$.twoFactorEnabled").value(false))
                        .andExpect(header().exists(AUTHORIZATION))
                        .andExpect(header().string(AUTHORIZATION, startsWith("Bearer ")));
            }
        }
    }

    @Nested
    class ShouldNotCreateUser {

        @Test
        void shouldNotCreateUserWithBlankFirstName() throws Exception {
            // Given
            CreateUserCommand createUserCommand = CreateUserCommand.builder()
                    .firstName("")
                    .lastName("Doe")
                    .username("johndoe")
                    .email("johndoe.kanwise@gmail.com")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'firstName' && @.message == 'FIRST_NAME_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateUserWithBlankLastName() throws Exception {
            // Given
            CreateUserCommand createUserCommand = CreateUserCommand.builder()
                    .firstName("John")
                    .lastName("")
                    .username("johndoe")
                    .email("johndoe.kanwise@gmail.com")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'lastName' && @.message == 'LAST_NAME_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateUserWithNotUniqueUsername() throws Exception {
            // Given
            String notUniqueUsername = "jargrave0";
            CreateUserCommand createUserCommand = CreateUserCommand.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .username(notUniqueUsername)
                    .email("johndoe.kanwise@gmail.com")
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.username").value(notUniqueUsername));
            // Then
            mockMvc.perform(post("/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_UNIQUE')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateUserWithNotUniqueEmail() throws Exception {
            // Given
            String notUniqueEmail = "jolettatiger.kanwise@gmail.com";
            CreateUserCommand createUserCommand = CreateUserCommand.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .username("johndoe")
                    .email(notUniqueEmail)
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.email").value(notUniqueEmail));
            // Then
            mockMvc.perform(post("/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'EMAIL_NOT_UNIQUE')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateUserWithInvalidEmail() throws Exception {
            // Given
            CreateUserCommand createUserCommand = CreateUserCommand.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .username("johndoe")
                    .email("johndoe.kanwise")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'INVALID_EMAIL_PATTERN')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateUserIfNotAuthorized() throws Exception {
            // Give
            CreateUserCommand createUserCommand = CreateUserCommand.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .username("johndoe")
                    .email("johndoe.kanwise@gmail.com")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/user")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserCommand)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("FULL_AUTHENTICATION_IS_REQUIRED_TO_ACCESS_THIS_RESOURCE"));
        }
    }

    @Nested
    class ShouldFindUser {
        @Test
        void shouldFindUser() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
        }
    }

    @Nested
    class ShouldNotFindUser {

        @Test
        void shouldNotFindUserIfUserDoesNotExist() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user/51")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"));
        }

        @Test
        void shouldNotFindUserIfNotAuthorized() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user/1"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("FULL_AUTHENTICATION_IS_REQUIRED_TO_ACCESS_THIS_RESOURCE"));
        }
    }

    @Nested
    class ShouldFindUsers {

        @Test
        void shouldFindUsersByUsernames() throws Exception {
            // Given
            List<String> usernames = List.of("jargrave0", "abeaston1d");
            // When
            // Then
            mockMvc.perform(get("/user?usernames={usernames}", usernames)
                            .param("usernames", usernames.toArray(String[]::new))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.[0].id").value(1))
                    .andExpect(jsonPath("$.[0].firstName").value("Joletta"))
                    .andExpect(jsonPath("$.[0].lastName").value("Tiger"))
                    .andExpect(jsonPath("$.[0].username").value("jargrave0"))
                    .andExpect(jsonPath("$.[0].email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.[0].userRole").value("USER"))
                    .andExpect(jsonPath("$.[0].lastLoginDate").exists())
                    .andExpect(jsonPath("$.[0].joinDate").exists())
                    .andExpect(jsonPath("$.[0].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.[0].phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.[1].id").value(50))
                    .andExpect(jsonPath("$.[1].firstName").value("Alidia"))
                    .andExpect(jsonPath("$.[1].lastName").value("Beaston"))
                    .andExpect(jsonPath("$.[1].username").value("abeaston1d"))
                    .andExpect(jsonPath("$.[1].email").value("alidiabeaston.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.[1].userRole").value("USER"))
                    .andExpect(jsonPath("$.[1].lastLoginDate").exists())
                    .andExpect(jsonPath("$.[1].joinDate").exists())
                    .andExpect(jsonPath("$.[1].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.[1].phoneNumber").value("+46 699 491 9032"))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        void shouldFindUsersWithDefaultPagination() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(5))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].firstName").value("Joletta"))
                    .andExpect(jsonPath("$.content[0].lastName").value("Tiger"))
                    .andExpect(jsonPath("$.content[0].username").value("jargrave0"))
                    .andExpect(jsonPath("$.content[0].email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[0].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[0].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[0].joinDate").exists())
                    .andExpect(jsonPath("$.content[0].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[0].phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.content[1].id").value(2))
                    .andExpect(jsonPath("$.content[1].firstName").value("Cully"))
                    .andExpect(jsonPath("$.content[1].lastName").value("Elders"))
                    .andExpect(jsonPath("$.content[1].username").value("celders1"))
                    .andExpect(jsonPath("$.content[1].email").value("cullyelders.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[1].userRole").value("ADMIN"))
                    .andExpect(jsonPath("$.content[1].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[1].joinDate").exists())
                    .andExpect(jsonPath("$.content[1].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[1].phoneNumber").value("+54 508 521 2350"))
                    .andExpect(jsonPath("$.content[2].id").value(3))
                    .andExpect(jsonPath("$.content[2].firstName").value("Bryan"))
                    .andExpect(jsonPath("$.content[2].lastName").value("Sabathier"))
                    .andExpect(jsonPath("$.content[2].username").value("bsabathier2"))
                    .andExpect(jsonPath("$.content[2].email").value("bryansabathier.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[2].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[2].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[2].joinDate").exists())
                    .andExpect(jsonPath("$.content[2].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[2].phoneNumber").value("+254 240 438 1332"))
                    .andExpect(jsonPath("$.content[3].id").value(4))
                    .andExpect(jsonPath("$.content[3].firstName").value("Abbey"))
                    .andExpect(jsonPath("$.content[3].lastName").value("Pinkie"))
                    .andExpect(jsonPath("$.content[3].username").value("apinkie3"))
                    .andExpect(jsonPath("$.content[3].email").value("abbeypinkie.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[3].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[3].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[3].joinDate").exists())
                    .andExpect(jsonPath("$.content[3].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[3].phoneNumber").value("+420 324 628 5949"))
                    .andExpect(jsonPath("$.content[4].id").value(5))
                    .andExpect(jsonPath("$.content[4].firstName").value("Birgitta"))
                    .andExpect(jsonPath("$.content[4].lastName").value("How"))
                    .andExpect(jsonPath("$.content[4].username").value("bhow4"))
                    .andExpect(jsonPath("$.content[4].email").value("birgittahow.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[4].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[4].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[4].joinDate").exists())
                    .andExpect(jsonPath("$.content[4].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[4].phoneNumber").value("+95 283 976 8939"))
                    .andExpect(jsonPath("$.content[5]").doesNotExist())
                    .andExpect(jsonPath("$.pageable.pageSize").value(5))
                    .andExpect(jsonPath("$.pageable.paged").value(true))
                    .andExpect(jsonPath("$.totalElements").value(50))
                    .andExpect(jsonPath("$.totalPages").value(10))
                    .andDo(print());
        }

        @Test
        void shouldFindUsersInAscendingOrder() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user?sortDirection=ASC")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(5))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].firstName").value("Joletta"))
                    .andExpect(jsonPath("$.content[0].lastName").value("Tiger"))
                    .andExpect(jsonPath("$.content[0].username").value("jargrave0"))
                    .andExpect(jsonPath("$.content[0].email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[0].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[0].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[0].joinDate").exists())
                    .andExpect(jsonPath("$.content[0].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[0].phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.content[1].id").value(2))
                    .andExpect(jsonPath("$.content[1].firstName").value("Cully"))
                    .andExpect(jsonPath("$.content[1].lastName").value("Elders"))
                    .andExpect(jsonPath("$.content[1].username").value("celders1"))
                    .andExpect(jsonPath("$.content[1].email").value("cullyelders.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[1].userRole").value("ADMIN"))
                    .andExpect(jsonPath("$.content[1].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[1].joinDate").exists())
                    .andExpect(jsonPath("$.content[1].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[1].phoneNumber").value("+54 508 521 2350"))
                    .andExpect(jsonPath("$.content[2].id").value(3))
                    .andExpect(jsonPath("$.content[2].firstName").value("Bryan"))
                    .andExpect(jsonPath("$.content[2].lastName").value("Sabathier"))
                    .andExpect(jsonPath("$.content[2].username").value("bsabathier2"))
                    .andExpect(jsonPath("$.content[2].email").value("bryansabathier.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[2].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[2].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[2].joinDate").exists())
                    .andExpect(jsonPath("$.content[2].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[2].phoneNumber").value("+254 240 438 1332"))
                    .andExpect(jsonPath("$.content[3].id").value(4))
                    .andExpect(jsonPath("$.content[3].firstName").value("Abbey"))
                    .andExpect(jsonPath("$.content[3].lastName").value("Pinkie"))
                    .andExpect(jsonPath("$.content[3].username").value("apinkie3"))
                    .andExpect(jsonPath("$.content[3].email").value("abbeypinkie.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[3].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[3].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[3].joinDate").exists())
                    .andExpect(jsonPath("$.content[3].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[3].phoneNumber").value("+420 324 628 5949"))
                    .andExpect(jsonPath("$.content[4].id").value(5))
                    .andExpect(jsonPath("$.content[4].firstName").value("Birgitta"))
                    .andExpect(jsonPath("$.content[4].lastName").value("How"))
                    .andExpect(jsonPath("$.content[4].username").value("bhow4"))
                    .andExpect(jsonPath("$.content[4].email").value("birgittahow.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[4].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[4].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[4].joinDate").exists())
                    .andExpect(jsonPath("$.content[4].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[4].phoneNumber").value("+95 283 976 8939"))
                    .andExpect(jsonPath("$.content[5]").doesNotExist())
                    .andExpect(jsonPath("$.pageable.pageSize").value(5))
                    .andExpect(jsonPath("$.pageable.paged").value(true))
                    .andExpect(jsonPath("$.totalElements").value(50))
                    .andExpect(jsonPath("$.totalPages").value(10))
                    .andDo(print());
        }

        @Test
        void shouldFindUsersInDescendingOrder() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user?sortDirection=DESC")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(5))
                    .andExpect(jsonPath("$.content[0].id").value(50))
                    .andExpect(jsonPath("$.content[0].firstName").value("Alidia"))
                    .andExpect(jsonPath("$.content[0].lastName").value("Beaston"))
                    .andExpect(jsonPath("$.content[0].username").value("abeaston1d"))
                    .andExpect(jsonPath("$.content[0].email").value("alidiabeaston.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[0].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[0].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[0].joinDate").exists())
                    .andExpect(jsonPath("$.content[0].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[0].phoneNumber").value("+46 699 491 9032"))
                    .andExpect(jsonPath("$.content[1].id").value(49))
                    .andExpect(jsonPath("$.content[1].firstName").value("Clerkclaude"))
                    .andExpect(jsonPath("$.content[1].lastName").value("Powderham"))
                    .andExpect(jsonPath("$.content[1].username").value("cpowderham1c"))
                    .andExpect(jsonPath("$.content[1].email").value("clerkclaudepowderham.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[1].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[1].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[1].joinDate").exists())
                    .andExpect(jsonPath("$.content[1].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[1].phoneNumber").value("+62 614 592 0959"))
                    .andExpect(jsonPath("$.content[2].id").value(48))
                    .andExpect(jsonPath("$.content[2].firstName").value("Harriett"))
                    .andExpect(jsonPath("$.content[2].lastName").value("Degoey"))
                    .andExpect(jsonPath("$.content[2].username").value("hdegoey1b"))
                    .andExpect(jsonPath("$.content[2].email").value("harriettdegoey.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[2].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[2].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[2].joinDate").exists())
                    .andExpect(jsonPath("$.content[2].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[2].phoneNumber").value("+62 467 469 1377"))
                    .andExpect(jsonPath("$.content[3].id").value(47))
                    .andExpect(jsonPath("$.content[3].firstName").value("Antonin"))
                    .andExpect(jsonPath("$.content[3].lastName").value("Pranger"))
                    .andExpect(jsonPath("$.content[3].username").value("apranger1a"))
                    .andExpect(jsonPath("$.content[3].email").value("antoninpranger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[3].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[3].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[3].joinDate").exists())
                    .andExpect(jsonPath("$.content[3].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[3].phoneNumber").value("+595 887 840 5724"))
                    .andExpect(jsonPath("$.content[4].id").value(46))
                    .andExpect(jsonPath("$.content[4].firstName").value("Herb"))
                    .andExpect(jsonPath("$.content[4].lastName").value("Blunsden"))
                    .andExpect(jsonPath("$.content[4].username").value("hblunsden19"))
                    .andExpect(jsonPath("$.content[4].email").value("herbblunsden.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[4].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[4].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[4].joinDate").exists())
                    .andExpect(jsonPath("$.content[4].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[4].phoneNumber").value("+358 133 657 6828"))
                    .andExpect(jsonPath("$.content[5].id").doesNotExist())
                    .andExpect(jsonPath("$.pageable.pageSize").value(5))
                    .andExpect(jsonPath("$.pageable.paged").value(true))
                    .andExpect(jsonPath("$.totalElements").value(50))
                    .andExpect(jsonPath("$.totalPages").value(10))
                    .andDo(print());
        }

        @Test
        void shouldFindUsersWithCustomPageSize() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user?pageSize=3")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(3))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].firstName").value("Joletta"))
                    .andExpect(jsonPath("$.content[0].lastName").value("Tiger"))
                    .andExpect(jsonPath("$.content[0].username").value("jargrave0"))
                    .andExpect(jsonPath("$.content[0].email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[0].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[0].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[0].joinDate").exists())
                    .andExpect(jsonPath("$.content[0].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[0].phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.content[1].id").value(2))
                    .andExpect(jsonPath("$.content[1].firstName").value("Cully"))
                    .andExpect(jsonPath("$.content[1].lastName").value("Elders"))
                    .andExpect(jsonPath("$.content[1].username").value("celders1"))
                    .andExpect(jsonPath("$.content[1].email").value("cullyelders.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[1].userRole").value("ADMIN"))
                    .andExpect(jsonPath("$.content[1].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[1].joinDate").exists())
                    .andExpect(jsonPath("$.content[1].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[1].phoneNumber").value("+54 508 521 2350"))
                    .andExpect(jsonPath("$.content[2].id").value(3))
                    .andExpect(jsonPath("$.content[2].firstName").value("Bryan"))
                    .andExpect(jsonPath("$.content[2].lastName").value("Sabathier"))
                    .andExpect(jsonPath("$.content[2].username").value("bsabathier2"))
                    .andExpect(jsonPath("$.content[2].email").value("bryansabathier.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[2].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[2].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[2].joinDate").exists())
                    .andExpect(jsonPath("$.content[2].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[2].phoneNumber").value("+254 240 438 1332"))
                    .andExpect(jsonPath("$.content[3]").doesNotExist())
                    .andExpect(jsonPath("$.pageable.pageSize").value(3))
                    .andExpect(jsonPath("$.pageable.paged").value(true))
                    .andExpect(jsonPath("$.totalElements").value(50))
                    .andExpect(jsonPath("$.totalPages").value(17))
                    .andDo(print());
        }

        @Test
        void shouldFindUsersWithCustomPageNumber() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user?pageNumber=1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(5))
                    .andExpect(jsonPath("$.content[0].id").value(6))
                    .andExpect(jsonPath("$.content[0].firstName").value("Carolyn"))
                    .andExpect(jsonPath("$.content[0].lastName").value("Wadwell"))
                    .andExpect(jsonPath("$.content[0].username").value("cwadwell5"))
                    .andExpect(jsonPath("$.content[0].email").value("carolynwadwell.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[0].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[0].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[0].joinDate").exists())
                    .andExpect(jsonPath("$.content[0].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[0].phoneNumber").value("+62 678 641 4568"))
                    .andExpect(jsonPath("$.content[1].id").value(7))
                    .andExpect(jsonPath("$.content[1].firstName").value("Lolita"))
                    .andExpect(jsonPath("$.content[1].lastName").value("Fantonetti"))
                    .andExpect(jsonPath("$.content[1].username").value("lfantonetti6"))
                    .andExpect(jsonPath("$.content[1].email").value("lolitafantonetti.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[1].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[1].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[1].joinDate").exists())
                    .andExpect(jsonPath("$.content[1].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[1].phoneNumber").value("+62 587 139 9065"))
                    .andExpect(jsonPath("$.content[2].id").value(8))
                    .andExpect(jsonPath("$.content[2].firstName").value("Karen"))
                    .andExpect(jsonPath("$.content[2].lastName").value("Tayt"))
                    .andExpect(jsonPath("$.content[2].username").value("ktayt7"))
                    .andExpect(jsonPath("$.content[2].email").value("karentayt.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[2].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[2].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[2].joinDate").exists())
                    .andExpect(jsonPath("$.content[2].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[2].phoneNumber").value("+420 710 419 9149"))
                    .andExpect(jsonPath("$.content[3].id").value(9))
                    .andExpect(jsonPath("$.content[3].firstName").value("Karen"))
                    .andExpect(jsonPath("$.content[3].lastName").value("Doget"))
                    .andExpect(jsonPath("$.content[3].username").value("kdoget8"))
                    .andExpect(jsonPath("$.content[3].email").value("karendoget.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[3].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[3].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[3].joinDate").exists())
                    .andExpect(jsonPath("$.content[3].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[3].phoneNumber").value("+86 411 439 5938"))
                    .andExpect(jsonPath("$.content[4].id").value(10))
                    .andExpect(jsonPath("$.content[4].firstName").value("Shirleen"))
                    .andExpect(jsonPath("$.content[4].lastName").value("Kemish"))
                    .andExpect(jsonPath("$.content[4].username").value("skemish9"))
                    .andExpect(jsonPath("$.content[4].email").value("shirleenkemish.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[4].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[4].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[4].joinDate").exists())
                    .andExpect(jsonPath("$.content[4].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[4].phoneNumber").value("+62 820 452 9051"))
                    .andExpect(jsonPath("$.content[5].id").doesNotExist())
                    .andExpect(jsonPath("$.pageable.pageSize").value(5))
                    .andExpect(jsonPath("$.pageable.paged").value(true))
                    .andExpect(jsonPath("$.totalElements").value(50))
                    .andExpect(jsonPath("$.totalPages").value(10))
                    .andDo(print());
        }

        @Test
        void shouldFindUsersWithCustomSortBy() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user?sortBy=lastName")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(5))
                    .andExpect(jsonPath("$.content[0].id").value(40))
                    .andExpect(jsonPath("$.content[0].firstName").value("Fancy"))
                    .andExpect(jsonPath("$.content[0].lastName").value("Alison"))
                    .andExpect(jsonPath("$.content[0].username").value("falison13"))
                    .andExpect(jsonPath("$.content[0].email").value("fancyalison.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[0].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[0].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[0].joinDate").exists())
                    .andExpect(jsonPath("$.content[0].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[0].phoneNumber").value("+82 329 902 2354"))
                    .andExpect(jsonPath("$.content[1].id").value(45))
                    .andExpect(jsonPath("$.content[1].firstName").value("Tisha"))
                    .andExpect(jsonPath("$.content[1].lastName").value("Atkins"))
                    .andExpect(jsonPath("$.content[1].username").value("tatkins18"))
                    .andExpect(jsonPath("$.content[1].email").value("tishaatkins.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[1].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[1].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[1].joinDate").exists())
                    .andExpect(jsonPath("$.content[1].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[1].phoneNumber").value("+63 101 276 3267"))
                    .andExpect(jsonPath("$.content[2].id").value(31))
                    .andExpect(jsonPath("$.content[2].firstName").value("Stefa"))
                    .andExpect(jsonPath("$.content[2].lastName").value("Barks"))
                    .andExpect(jsonPath("$.content[2].username").value("sbarksu"))
                    .andExpect(jsonPath("$.content[2].email").value("stefabarks.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[2].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[2].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[2].joinDate").exists())
                    .andExpect(jsonPath("$.content[2].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[2].phoneNumber").value("+54 358 150 2635"))
                    .andExpect(jsonPath("$.content[3].id").value(50))
                    .andExpect(jsonPath("$.content[3].firstName").value("Alidia"))
                    .andExpect(jsonPath("$.content[3].lastName").value("Beaston"))
                    .andExpect(jsonPath("$.content[3].username").value("abeaston1d"))
                    .andExpect(jsonPath("$.content[3].email").value("alidiabeaston.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[3].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[3].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[3].joinDate").exists())
                    .andExpect(jsonPath("$.content[3].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[3].phoneNumber").value("+46 699 491 9032"))
                    .andExpect(jsonPath("$.content[4].id").value(46))
                    .andExpect(jsonPath("$.content[4].firstName").value("Herb"))
                    .andExpect(jsonPath("$.content[4].lastName").value("Blunsden"))
                    .andExpect(jsonPath("$.content[4].username").value("hblunsden19"))
                    .andExpect(jsonPath("$.content[4].email").value("herbblunsden.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[4].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[4].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[4].joinDate").exists())
                    .andExpect(jsonPath("$.content[4].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[4].phoneNumber").value("+358 133 657 6828"))
                    .andExpect(jsonPath("$.content[5]").doesNotExist())
                    .andExpect(jsonPath("$.pageable.pageSize").value(5))
                    .andExpect(jsonPath("$.pageable.paged").value(true))
                    .andExpect(jsonPath("$.totalElements").value(50))
                    .andExpect(jsonPath("$.totalPages").value(10))
                    .andDo(print());
        }

        @Test
        void shouldFindUsersWithCustomPaginationParameters() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user?pageNumber=2&pageSize=4&sortBy=firstName&sortDirection=DESC")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(4))
                    .andExpect(jsonPath("$.content[0].id").value(42))
                    .andExpect(jsonPath("$.content[0].firstName").value("Rikki"))
                    .andExpect(jsonPath("$.content[0].lastName").value("Connors"))
                    .andExpect(jsonPath("$.content[0].username").value("rconnors15"))
                    .andExpect(jsonPath("$.content[0].email").value("rikkiconnors.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[0].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[0].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[0].joinDate").exists())
                    .andExpect(jsonPath("$.content[0].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[0].phoneNumber").value("+235 702 296 9514"))
                    .andExpect(jsonPath("$.content[1].id").value(19))
                    .andExpect(jsonPath("$.content[1].firstName").value("Penny"))
                    .andExpect(jsonPath("$.content[1].lastName").value("Thorp"))
                    .andExpect(jsonPath("$.content[1].username").value("pthorpi"))
                    .andExpect(jsonPath("$.content[1].email").value("pennythorp.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[1].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[1].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[1].joinDate").exists())
                    .andExpect(jsonPath("$.content[1].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[1].phoneNumber").value("+63 593 898 8572"))
                    .andExpect(jsonPath("$.content[2].id").value(17))
                    .andExpect(jsonPath("$.content[2].firstName").value("Orland"))
                    .andExpect(jsonPath("$.content[2].lastName").value("Scouse"))
                    .andExpect(jsonPath("$.content[2].username").value("oscouseg"))
                    .andExpect(jsonPath("$.content[2].email").value("orlandscouse.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[2].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[2].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[2].joinDate").exists())
                    .andExpect(jsonPath("$.content[2].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[2].phoneNumber").value("+62 704 392 8424"))
                    .andExpect(jsonPath("$.content[3].id").value(22))
                    .andExpect(jsonPath("$.content[3].firstName").value("Olympia"))
                    .andExpect(jsonPath("$.content[3].lastName").value("Pavitt"))
                    .andExpect(jsonPath("$.content[3].username").value("opavittl"))
                    .andExpect(jsonPath("$.content[3].email").value("olympiapavitt.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[3].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[3].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[3].joinDate").exists())
                    .andExpect(jsonPath("$.content[3].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[3].phoneNumber").value("+86 710 227 9630"))
                    .andExpect(jsonPath("$.pageable.pageSize").value(4))
                    .andExpect(jsonPath("$.pageable.paged").value(true))
                    .andExpect(jsonPath("$.totalElements").value(50))
                    .andExpect(jsonPath("$.totalPages").value(13))
                    .andDo(print());
        }

        @Test
        void shouldFindUserByLastName() throws Exception {
            // Given
            String lastName = "Kemish";
            // When
            // Then
            mockMvc.perform(get("/user?lastName=" + lastName)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(3))
                    .andExpect(jsonPath("$.content[0].id").value(10))
                    .andExpect(jsonPath("$.content[0].firstName").value("Shirleen"))
                    .andExpect(jsonPath("$.content[0].lastName").value(lastName))
                    .andExpect(jsonPath("$.content[0].username").value("skemish9"))
                    .andExpect(jsonPath("$.content[0].email").value("shirleenkemish.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[0].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[0].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[0].joinDate").exists())
                    .andExpect(jsonPath("$.content[0].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[0].phoneNumber").value("+62 820 452 9051"))
                    .andExpect(jsonPath("$.content[1].id").value(12))
                    .andExpect(jsonPath("$.content[1].firstName").value("Gavan"))
                    .andExpect(jsonPath("$.content[1].lastName").value(lastName))
                    .andExpect(jsonPath("$.content[1].username").value("gprisleyb"))
                    .andExpect(jsonPath("$.content[1].email").value("gavankemish.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[1].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[1].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[1].joinDate").exists())
                    .andExpect(jsonPath("$.content[1].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[1].phoneNumber").value("+420 269 853 1016"))
                    .andExpect(jsonPath("$.content[2].id").value(25))
                    .andExpect(jsonPath("$.content[2].firstName").value("Terza"))
                    .andExpect(jsonPath("$.content[2].lastName").value(lastName))
                    .andExpect(jsonPath("$.content[2].username").value("trauno"))
                    .andExpect(jsonPath("$.content[2].email").value("terzakemish.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.content[2].userRole").value("USER"))
                    .andExpect(jsonPath("$.content[2].lastLoginDate").exists())
                    .andExpect(jsonPath("$.content[2].joinDate").exists())
                    .andExpect(jsonPath("$.content[2].twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.content[2].phoneNumber").value("+1 219 372 0822"))
                    .andExpect(jsonPath("$.pageable.pageSize").value(5))
                    .andExpect(jsonPath("$.pageable.paged").value(true))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotFindUsers {

        @Test
        void shouldNotFindUsersIfNotAuthorized() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("FULL_AUTHENTICATION_IS_REQUIRED_TO_ACCESS_THIS_RESOURCE"));
        }


        @ValueSource(ints = {Integer.MIN_VALUE, -100, -1, 0})
        @ParameterizedTest
        void shouldNotFindUsersWithInvalidPageSize(int pageSize) throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user?pageSize=" + pageSize)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'pageSize' && @.message == 'PAGE_SIZE_NOT_LESS_THAN_ONE')]").exists())
                    .andDo(print());
        }

        @ValueSource(ints = {Integer.MIN_VALUE, -100, -1})
        @ParameterizedTest
        void shouldNotFindUsersWithInvalidPageNumber(int pageNumber) throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user?pageNumber=" + pageNumber)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'pageNumber' && @.message == 'PAGE_NUMBER_NOT_NEGATIVE')]").exists())
                    .andDo(print());
        }

        @ValueSource(strings = {"", " ", "firstN", "lName", "usernme"})
        @ParameterizedTest
        void shouldNotFindUsersWithInvalidSortBy(String sortBy) throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user?sortBy=" + sortBy)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'sortBy' && @.message == 'INVALID_SORT_BY_VALUE')]").exists())
                    .andDo(print());
        }


        @ValueSource(strings = {"", " ", "sac", "edsc"})
        @ParameterizedTest
        void shouldNotFindUsersWithInvalidOrder(String sortDirection) throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user?sortDirection=" + sortDirection)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'sortDirection' && @.message == 'INVALID_SORT_DIRECTION')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotFindUsersWithInvalidPaginationParameters() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/user?sortDirection=acs&sortBy=firstNam&pageSize=0&pageNumber=-1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'sortDirection' && @.message == 'INVALID_SORT_DIRECTION')]").exists())
                    .andExpect(jsonPath("$.[?(@.field == 'sortBy' && @.message == 'INVALID_SORT_BY_VALUE')]").exists())
                    .andExpect(jsonPath("$.[?(@.field == 'pageSize' && @.message == 'PAGE_SIZE_NOT_LESS_THAN_ONE')]").exists())
                    .andExpect(jsonPath("$.[?(@.field == 'pageNumber' && @.message == 'PAGE_NUMBER_NOT_NEGATIVE')]").exists())
                    .andDo(print());
        }
    }

    @Nested
    class ShouldEditUser {
        @Test
        void shouldEditUser() throws Exception {
            // Given
            EditUserCommand editUserCommand = EditUserCommand.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("johnnydoe.kanwise@gmail.com")
                    .username("johnnydoe")
                    .notificationSubscriptions(Map.of(USER_UPDATED, false,
                            PASSWORD_UPDATED, false,
                            PASSWORD_RESET, false,
                            USER_BLOCKED, false,
                            USER_UNBLOCKED, false,
                            USER_DELETED, false))
                    .twoFactorSubscriptions(Map.of(
                            LOGIN, false,
                            RESET_PASSWORD, false,
                            CHANGE_PASSWORD, false))
                    .twoFactorEnabled(true)
                    .phoneNumber("+46 114 214 2101")
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.notificationSubscriptions").exists())
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PASSWORD_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PASSWORD_RESET").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_BLOCKED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_UNBLOCKED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_DELETED").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(true));
            // Then
            mockMvc.perform(put("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserCommand)
                            ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value(editUserCommand.firstName()))
                    .andExpect(jsonPath("$.lastName").value(editUserCommand.lastName()))
                    .andExpect(jsonPath("$.username").value(editUserCommand.username()))
                    .andExpect(jsonPath("$.email").value(editUserCommand.email()))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(editUserCommand.twoFactorEnabled()))
                    .andExpect(jsonPath("$.phoneNumber").value(editUserCommand.phoneNumber()))
                    .andExpect(jsonPath("$.notificationSubscriptions").exists())
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_UPDATED").value(editUserCommand.notificationSubscriptions().get(USER_UPDATED)))
                    .andExpect(jsonPath("$.notificationSubscriptions.PASSWORD_UPDATED").value(editUserCommand.notificationSubscriptions().get(PASSWORD_UPDATED)))
                    .andExpect(jsonPath("$.notificationSubscriptions.PASSWORD_RESET").value(editUserCommand.notificationSubscriptions().get(PASSWORD_RESET)))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_BLOCKED").value(editUserCommand.notificationSubscriptions().get(USER_BLOCKED)))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_UNBLOCKED").value(editUserCommand.notificationSubscriptions().get(USER_UNBLOCKED)))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_DELETED").value(editUserCommand.notificationSubscriptions().get(USER_DELETED)))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(editUserCommand.twoFactorSubscriptions().get(LOGIN)))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(editUserCommand.twoFactorSubscriptions().get(RESET_PASSWORD)))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(editUserCommand.twoFactorSubscriptions().get(CHANGE_PASSWORD)));

            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value(editUserCommand.firstName()))
                    .andExpect(jsonPath("$.lastName").value(editUserCommand.lastName()))
                    .andExpect(jsonPath("$.username").value(editUserCommand.username()))
                    .andExpect(jsonPath("$.email").value(editUserCommand.email()))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(editUserCommand.twoFactorEnabled()))
                    .andExpect(jsonPath("$.phoneNumber").value(editUserCommand.phoneNumber()))
                    .andExpect(jsonPath("$.notificationSubscriptions").exists())
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_UPDATED").value(editUserCommand.notificationSubscriptions().get(USER_UPDATED)))
                    .andExpect(jsonPath("$.notificationSubscriptions.PASSWORD_UPDATED").value(editUserCommand.notificationSubscriptions().get(PASSWORD_UPDATED)))
                    .andExpect(jsonPath("$.notificationSubscriptions.PASSWORD_RESET").value(editUserCommand.notificationSubscriptions().get(PASSWORD_RESET)))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_BLOCKED").value(editUserCommand.notificationSubscriptions().get(USER_BLOCKED)))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_UNBLOCKED").value(editUserCommand.notificationSubscriptions().get(USER_UNBLOCKED)))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_DELETED").value(editUserCommand.notificationSubscriptions().get(USER_DELETED)))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(editUserCommand.twoFactorSubscriptions().get(LOGIN)))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(editUserCommand.twoFactorSubscriptions().get(RESET_PASSWORD)))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(editUserCommand.twoFactorSubscriptions().get(CHANGE_PASSWORD)));
        }
    }

    @Nested
    class ShouldNotEditUser {

        @Test
        void shouldNotEditUserIfNotAuthorized() throws Exception {
            // Given
            EditUserCommand editUserCommand = EditUserCommand.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("johnnydoe.kanwise@gmail.com")
                    .username("johnnydoe")
                    .notificationSubscriptions(Map.of())
                    .twoFactorSubscriptions(Map.of())
                    .twoFactorEnabled(true)
                    .phoneNumber("+1 219 372 0822")
                    .build();
            // When
            // Then
            mockMvc.perform(put("/user/1")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserCommand)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("FULL_AUTHENTICATION_IS_REQUIRED_TO_ACCESS_THIS_RESOURCE"));
        }

        @Test
        void shouldNotEditUserIfUserDoesNotExist() throws Exception {
            // Given
            EditUserCommand editUserCommand = EditUserCommand.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("johnnydoe.kanwise@gmail.com")
                    .username("johnnydoe")
                    .notificationSubscriptions(Map.of())
                    .twoFactorSubscriptions(Map.of())
                    .twoFactorEnabled(true)
                    .phoneNumber("+1 219 372 0822")
                    .build();
            long userId = 51L;
            // When
            mockMvc.perform(get("/user/" + userId)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"));
            // Then
            mockMvc.perform(put("/user/" + userId)
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"));
        }
    }

    @Nested
    class ShouldEditUserPartially {
        @Test
        void shouldEditFirstName() throws Exception {
            // Given
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand.builder()
                    .firstName("John")
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
            // Then
            mockMvc.perform(patch("/user/1")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value(editUserPartiallyCommand.firstName()))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));

            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value(editUserPartiallyCommand.firstName()))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
        }

        @Test
        void shouldEditLastName() throws Exception {
            // Given
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand.builder()
                    .lastName("Doe")
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
            // Then
            mockMvc.perform(patch("/user/1")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value(editUserPartiallyCommand.lastName()))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));

            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value(editUserPartiallyCommand.lastName()))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
        }

        @Test
        void shouldEditEmail() throws Exception {
            // Given
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand.builder()
                    .email("jola.kanwise@gmail.com")
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
            // Then
            mockMvc.perform(patch("/user/1")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value(editUserPartiallyCommand.email()))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));

            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value(editUserPartiallyCommand.email()))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
        }

        @Test
        void shouldEditUsername() throws Exception {
            // Given
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand.builder()
                    .username("tigerjola")
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
            // Then
            mockMvc.perform(patch("/user/1")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(editUserPartiallyCommand.username()))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));

            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(editUserPartiallyCommand.username()))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
        }

        @Test
        void shouldEditNotificationSubscriptions() throws Exception {
            // Given
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand.builder()
                    .notificationSubscriptions(Map.of(
                            USER_UPDATED, false,
                            PASSWORD_UPDATED, false,
                            PASSWORD_RESET, false,
                            USER_BLOCKED, false,
                            USER_UNBLOCKED, false,
                            USER_DELETED, false))
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.notificationSubscriptions").exists())
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PASSWORD_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PASSWORD_RESET").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_BLOCKED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_UNBLOCKED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_DELETED").value(true));


            // Then
            mockMvc.perform(patch("/user/1")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.notificationSubscriptions").exists())
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_UPDATED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PASSWORD_UPDATED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PASSWORD_RESET").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_BLOCKED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_UNBLOCKED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_DELETED").value(false));


            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.notificationSubscriptions").exists())
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_UPDATED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PASSWORD_UPDATED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PASSWORD_RESET").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_BLOCKED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_UNBLOCKED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.USER_DELETED").value(false));
        }

        @Test
        void shouldEditTwoFactorSubscriptions() throws Exception {
            // Given
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand.builder()
                    .twoFactorSubscriptions(Map.of(
                            LOGIN, false,
                            RESET_PASSWORD, false,
                            CHANGE_PASSWORD, false))
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(true));
            // Then
            mockMvc.perform(patch("/user/1")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(false))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(false))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(false));

            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(false))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(false))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(false));
        }
    }

    @Nested
    class ShouldNotEditUserPartially {

        @Test
        void shouldNotEditUserPartiallyIfNotAuthorized() throws Exception {
            // Given
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand
                    .builder()
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/user/1")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value(FORBIDDEN.getReasonPhrase().toUpperCase()))
                    .andExpect(jsonPath("$.message").value("FULL_AUTHENTICATION_IS_REQUIRED_TO_ACCESS_THIS_RESOURCE"));
        }

        @Test
        void shouldNotEditUserPartiallyIfUserDoesNotExist() throws Exception {
            // Given
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand
                    .builder()
                    .build();
            long userId = 51L;
            // When
            mockMvc.perform(get("/user/" + userId)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"));
            // Then
            mockMvc.perform(patch("/user/" + userId)
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"));
        }

        @Test
        void shouldNotEditUserPartiallyWithNotUniqueUsername() throws Exception {
            // Given
            String notUniqueUsername = "jargrave0";
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand
                    .builder()
                    .username(notUniqueUsername)
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value(notUniqueUsername))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(true));
            // Then
            mockMvc.perform(patch("/user/2")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_NOT_UNIQUE"));

            mockMvc.perform(get("/user/2")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.firstName").value("Cully"))
                    .andExpect(jsonPath("$.lastName").value("Elders"))
                    .andExpect(jsonPath("$.username").value(not(notUniqueUsername)))
                    .andExpect(jsonPath("$.email").value("cullyelders.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("ADMIN"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+54 508 521 2350"));
        }

        @Test
        void shouldNotEditUserPartiallyWithNotUniqueEmail() throws Exception {
            // Given
            String notUniqueEmail = "jolettatiger.kanwise@gmail.com";
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand
                    .builder()
                    .email(notUniqueEmail)
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value(notUniqueEmail))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(true));
            // Then
            mockMvc.perform(patch("/user/2")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("EMAIL_NOT_UNIQUE"));

            mockMvc.perform(get("/user/2")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.firstName").value("Cully"))
                    .andExpect(jsonPath("$.lastName").value("Elders"))
                    .andExpect(jsonPath("$.username").value("celders1"))
                    .andExpect(jsonPath("$.email").value(not(notUniqueEmail)))
                    .andExpect(jsonPath("$.userRole").value("ADMIN"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+54 508 521 2350"));
        }

        @Test
        void shouldNotEditUserPartiallyWithInvalidEmail() throws Exception {
            // Given
            String invalidEmail = "invalidEmail";
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand
                    .builder()
                    .email(invalidEmail)
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(true));
            // Then
            mockMvc.perform(patch("/user/1")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'INVALID_EMAIL_PATTERN')]").exists())
                    .andDo(print());

            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value(not(invalidEmail)))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(true));
        }


        @Test
        void shouldNotEditUserPartiallyWithInvalidPhoneNumber() throws Exception {
            // Given
            String invalidPhoneNumber = "invalidPhoneNumber";
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand
                    .builder()
                    .phoneNumber(invalidPhoneNumber)
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value(not(invalidPhoneNumber)))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(true));
            // Then
            mockMvc.perform(patch("/user/1")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'phoneNumber' && @.message == 'INVALID_PHONE_NUMBER_PATTERN')]").exists())
                    .andDo(print());

            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value(not(invalidPhoneNumber)))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(true));
        }

        @Test
        void shouldNotEditUserPartiallyWithNotUniquePhoneNumber() throws Exception {
            // Given
            String notUniquePhoneNumber = "+46 114 204 2101";
            EditUserPartiallyCommand editUserPartiallyCommand = EditUserPartiallyCommand
                    .builder()
                    .phoneNumber(notUniquePhoneNumber)
                    .build();
            // When
            mockMvc.perform(get("/user/1")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value(notUniquePhoneNumber))
                    .andExpect(jsonPath("$.twoFactorSubscriptions").exists())
                    .andExpect(jsonPath("$.twoFactorSubscriptions.LOGIN").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.RESET_PASSWORD").value(true))
                    .andExpect(jsonPath("$.twoFactorSubscriptions.CHANGE_PASSWORD").value(true));
            // Then
            mockMvc.perform(patch("/user/2")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editUserPartiallyCommand))
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("PHONE_NUMBER_NOT_UNIQUE"));

            mockMvc.perform(get("/user/2")
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.firstName").value("Cully"))
                    .andExpect(jsonPath("$.lastName").value("Elders"))
                    .andExpect(jsonPath("$.username").value("celders1"))
                    .andExpect(jsonPath("$.email").value("cullyelders.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("ADMIN"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value(not(notUniquePhoneNumber)));
        }
    }

    @Nested
    class ShouldDeleteUser {
        @Test
        void shouldDeleteUser() throws Exception {
            // Given
            long userId = 1L;
            // When
            mockMvc.perform(get("/user/" + userId)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.firstName").value("Joletta"))
                    .andExpect(jsonPath("$.lastName").value("Tiger"))
                    .andExpect(jsonPath("$.username").value("jargrave0"))
                    .andExpect(jsonPath("$.email").value("jolettatiger.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.userRole").value("USER"))
                    .andExpect(jsonPath("$.lastLoginDate").exists())
                    .andExpect(jsonPath("$.joinDate").exists())
                    .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andExpect(jsonPath("$.phoneNumber").value("+46 114 204 2101"));
            // Then
            mockMvc.perform(delete("/user/" + userId)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isNoContent())
                    .andDo(print());
            mockMvc.perform(get("/user/" + userId)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"));
        }
    }

    @Nested
    class ShouldNotDeleteUser {

        @Test
        void shouldNotDeleteUserIfUserDoesNotExist() throws Exception {
            // Given
            long userId = 51L;
            // When
            mockMvc.perform(get("/user/" + userId)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"));
            // Then
            mockMvc.perform(delete("/user/" + userId)
                            .header(AUTHORIZATION, getAdminAuthorizationHeader()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"));
        }

        @Test
        void shouldNotDeleteUserIfNotAuthorized() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(delete("/user/1"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(FORBIDDEN.value()))
                    .andExpect(jsonPath("$.httpStatus").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("FULL_AUTHENTICATION_IS_REQUIRED_TO_ACCESS_THIS_RESOURCE"));
        }
    }
}