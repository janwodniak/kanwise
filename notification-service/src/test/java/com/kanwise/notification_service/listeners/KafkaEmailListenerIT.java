package com.kanwise.notification_service.listeners;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.kanwise.notification_service.TestPayload;
import com.kanwise.notification_service.configuration.kafka.common.KafkaConfigurationProperties;
import com.kanwise.notification_service.model.email.EmailMessageType;
import com.kanwise.notification_service.model.email.EmailRequest;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.icegreen.greenmail.configuration.GreenMailConfiguration.aConfig;
import static com.icegreen.greenmail.util.ServerSetupTest.SMTP;
import static com.kanwise.notification_service.listeners.KafkaTestingUtils.getKafkaProducerProperties;
import static com.kanwise.notification_service.model.email.EmailMessageType.ACCOUNT_BLOCKED;
import static com.kanwise.notification_service.model.email.EmailMessageType.ACCOUNT_CREATED;
import static com.kanwise.notification_service.model.email.EmailMessageType.NEW_TASK_ASSIGNED;
import static com.kanwise.notification_service.model.email.EmailMessageType.PASSWORD_CHANGED;
import static com.kanwise.notification_service.model.email.EmailMessageType.PASSWORD_RESET;
import static com.kanwise.notification_service.model.email.EmailMessageType.PERSONAL_REPORT;
import static com.kanwise.notification_service.model.email.EmailMessageType.PROJECT_JOIN_REQUEST_ACCEPTED;
import static com.kanwise.notification_service.model.email.EmailMessageType.PROJECT_JOIN_REQUEST_REJECTED;
import static com.kanwise.notification_service.model.email.EmailMessageType.PROJECT_REPORT;
import static com.kanwise.notification_service.model.email.EmailMessageType.USER_INFORMATION_CHANGED;
import static com.kanwise.notification_service.model.kafka.TopicType.NOTIFICATION_EMAIL;
import static java.time.Duration.ofHours;
import static java.time.LocalDateTime.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.clients.admin.AdminClient.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.kafka.config.TopicBuilder.name;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@DisplayName("Test email message request listener")
@DirtiesContext
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class KafkaEmailListenerIT {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(SMTP)
            .withConfiguration(aConfig().withUser("kanwise@gmail.com", "kanwise"))
            .withPerMethodLifecycle(true);
    private final AdminClient kafkaAdminClient;
    private final KafkaConfigurationProperties kafkaConfigurationProperties;
    private final KafkaProducer<String, EmailRequest> kafkaEmailRequestProducer;

    @Autowired
    public KafkaEmailListenerIT(KafkaAdmin kafkaAdmin, KafkaConfigurationProperties kafkaConfigurationProperties, KafkaProducer<String, EmailRequest> kafkaEmailRequestProducer) {
        this.kafkaAdminClient = create(kafkaAdmin.getConfigurationProperties());
        this.kafkaConfigurationProperties = kafkaConfigurationProperties;
        this.kafkaEmailRequestProducer = kafkaEmailRequestProducer;
    }


    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeAll
    static void beforeAll() {
        kafkaContainer.start();
    }

    @AfterAll
    static void tearDown() {
        kafkaContainer.close();
    }

    @BeforeEach
    void setUp() {
        kafkaAdminClient.createTopics(List.of(name(kafkaConfigurationProperties.getTopicName(NOTIFICATION_EMAIL)).build()));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public KafkaProducer<String, EmailRequest> kafkaEmailRequestProducer() {
            return new KafkaProducer<>(getKafkaProducerProperties(kafkaContainer));
        }
    }

    @DisplayName("Should consume email notification request and send email")
    @Nested
    class ShouldConsumeEmailNotificationRequestAndSendEmail {

        public static Stream<Arguments> emailRequestsArguments() {
            String testEmail = "john.kanwise@gmail.com";
            return Stream.of(Arguments.of(Named.of("Account created",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(ACCOUNT_CREATED)
                                            .data(Map.of())
                                            .isHtml(false)
                                            .subject("Account created")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("Account blocked",
                                    TestPayload.builder()
                                            .payload(EmailRequest.builder()
                                                    .to(testEmail)
                                                    .type(ACCOUNT_BLOCKED)
                                                    .data(Map.of())
                                                    .isHtml(false)
                                                    .subject("Account blocked")
                                                    .build()
                                            )
                                            .build()
                            )
                    ),
                    Arguments.of(Named.of("Project join request rejected",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(PROJECT_JOIN_REQUEST_REJECTED)
                                            .data(Map.of())
                                            .isHtml(false)
                                            .subject("Project join request rejected")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("Project join request accepted",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(PROJECT_JOIN_REQUEST_ACCEPTED)
                                            .data(Map.of())
                                            .isHtml(false)
                                            .subject("Project join request accepted")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("New task assigned",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(NEW_TASK_ASSIGNED)
                                            .data(Map.of())
                                            .isHtml(false)
                                            .subject("New task assigned")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("Password reset",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(PASSWORD_RESET)
                                            .data(Map.of())
                                            .isHtml(false)
                                            .subject("Password reset")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("Password changed",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(PASSWORD_CHANGED)
                                            .data(Map.of())
                                            .isHtml(false)
                                            .subject("Password changed")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("User information changed",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(USER_INFORMATION_CHANGED)
                                            .data(Map.of())
                                            .isHtml(false)
                                            .subject("User information changed")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("Personal report",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(PERSONAL_REPORT)
                                            .data(Map.of())
                                            .isHtml(false)
                                            .subject("Personal report")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("Project report",
                                    TestPayload.builder()
                                            .payload(EmailRequest.builder()
                                                    .to(testEmail)
                                                    .type(PROJECT_REPORT)
                                                    .data(Map.of())
                                                    .isHtml(false)
                                                    .subject("Project report")
                                                    .build()
                                            )
                                            .build()
                            )
                    ));
        }

        @MethodSource("emailRequestsArguments")
        @ParameterizedTest
        void shouldConsumeEmailNotificationRequestAndSendEmail(TestPayload<EmailRequest> testPayload) {
            // Given
            String topic = kafkaConfigurationProperties.getTopicName(NOTIFICATION_EMAIL);
            EmailRequest emailRequest = testPayload.payload();
            // When
            kafkaEmailRequestProducer.send(new ProducerRecord<>(topic, emailRequest));
            // Then
            await().atMost(2, SECONDS).untilAsserted(() -> {
                // Then
                MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                assertEquals(1, receivedMessages.length);
                assertEquals(emailRequest.getTo(), receivedMessages[0].getAllRecipients()[0].toString());
                assertEquals(emailRequest.getSubject(), receivedMessages[0].getSubject());
            });
        }
    }


    @DisplayName("Should consume email notification request and send html email")
    @Nested
    class ShouldConsumeEmailNotificationRequestAndSendHtmlEmail {

        public static Stream<Arguments> emailRequestsArguments() {
            String testEmail = "john.kanwise@gmail.com";
            return Stream.of(Arguments.of(Named.of("Account created",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(ACCOUNT_CREATED)
                                            .data(Map.of(
                                                    "firstName", "John",
                                                    "password", "password"
                                            ))
                                            .isHtml(true)
                                            .subject("Account created")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("Account blocked",
                                    TestPayload.builder()
                                            .payload(EmailRequest.builder()
                                                    .to(testEmail)
                                                    .type(ACCOUNT_BLOCKED)
                                                    .data(Map.of(
                                                            "contactAdministratorUrl", "http://localhost:8080/contact-administrator"
                                                    ))
                                                    .isHtml(true)
                                                    .subject("Account blocked")
                                                    .build()
                                            )
                                            .build()
                            )
                    ),
                    Arguments.of(Named.of("Project join request rejected",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(PROJECT_JOIN_REQUEST_REJECTED)
                                            .data(Map.of(
                                                    "firstName", "John",
                                                    "projectTitle", "Project title"
                                            ))
                                            .isHtml(true)
                                            .subject("Project join request rejected")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("Project join request accepted",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(PROJECT_JOIN_REQUEST_ACCEPTED)
                                            .data(Map.of(
                                                    "firstName", "John",
                                                    "projectTitle", "Project title"
                                            ))
                                            .isHtml(true)
                                            .subject("Project join request accepted")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("New task assigned",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(NEW_TASK_ASSIGNED)
                                            .data(Map.of(
                                                    "firstName", "John",
                                                    "projectTitle", "Project title",
                                                    "taskTitle", "Task title",
                                                    "taskType", "Task type",
                                                    "estimatedTime", ofHours(1),
                                                    "assignedAt", of(2022, 1, 1, 1, 1, 1, 1)
                                            ))
                                            .isHtml(true)
                                            .subject("New task assigned")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("Password reset",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(PASSWORD_RESET)
                                            .data(Map.of(
                                                    "firstName", "John",
                                                    "resetPasswordUrl", "http://localhost:8080/reset-password"
                                            ))
                                            .isHtml(true)
                                            .subject("Password reset")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("Password changed",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(PASSWORD_CHANGED)
                                            .data(Map.of(
                                                    "firstName", "John",
                                                    "contactAdministratorUrl", "http://localhost:8080/contact-administrator"
                                            ))
                                            .isHtml(true)
                                            .subject("Password changed")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("User information changed",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(USER_INFORMATION_CHANGED)
                                            .data(Map.of())
                                            .isHtml(true)
                                            .subject("User information changed")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("Personal report",
                            TestPayload.builder()
                                    .payload(EmailRequest.builder()
                                            .to(testEmail)
                                            .type(PERSONAL_REPORT)
                                            .data(Map.of(
                                                    "firstName", "John",
                                                    "projectTitle", "Project title",
                                                    "reportStartDate", of(2022, 1, 1, 0, 0, 0, 0),
                                                    "reportEndDate", of(2022, 1, 5, 0, 0, 0, 0),
                                                    "reportType", "Report type",
                                                    "href", "http://localhost:8080/report"
                                            ))
                                            .isHtml(true)
                                            .subject("Personal report")
                                            .build()
                                    )
                                    .build()
                    )),
                    Arguments.of(Named.of("Project report",
                                    TestPayload.builder()
                                            .payload(EmailRequest.builder()
                                                    .to(testEmail)
                                                    .type(PROJECT_REPORT)
                                                    .data(Map.of(
                                                            "firstName", "John",
                                                            "projectTitle", "Project title",
                                                            "reportStartDate", of(2022, 1, 1, 0, 0, 0, 0),
                                                            "reportEndDate", of(2022, 1, 5, 0, 0, 0, 0),
                                                            "reportType", "Report type",
                                                            "href", "http://localhost:8080/report"
                                                    ))
                                                    .isHtml(true)
                                                    .subject("Project report")
                                                    .build()
                                            )
                                            .build()
                            )
                    ));
        }

        @MethodSource("emailRequestsArguments")
        @ParameterizedTest
        void shouldConsumeEmailNotificationRequestAndHtmlSendEmail(TestPayload<EmailRequest> testPayload) throws Exception {
            // Given
            String topic = kafkaConfigurationProperties.getTopicName(NOTIFICATION_EMAIL);
            EmailRequest emailRequest = testPayload.payload();
            String expectedEmailContent = getHtmlFromFile(emailRequest.getType());
            // When
            kafkaEmailRequestProducer.send(new ProducerRecord<>(topic, emailRequest));
            // Then
            await().atMost(2, SECONDS).untilAsserted(() -> {
                // Then
                MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
                assertEquals(1, receivedMessages.length);
                assertEquals(emailRequest.getTo(), receivedMessages[0].getAllRecipients()[0].toString());
                assertEquals(emailRequest.getSubject(), receivedMessages[0].getSubject());
                String content = (String) receivedMessages[0].getContent();
                assertEquals(expectedEmailContent, content);
            });
        }

        private String getHtmlFromFile(EmailMessageType emailMessageType) throws IOException {
            return Files.readString(Path.of("src/test/resources/templates_test/" + emailMessageType.name().toLowerCase().replace("_", "-") + "-test.html"));
        }
    }
}