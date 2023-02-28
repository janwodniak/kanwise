package com.kanwise.notification_service.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.kanwise.clients.user_service.authentication.model.OtpSmsResponse;
import com.kanwise.notification_service.configuration.kafka.common.KafkaConfigurationProperties;
import com.kanwise.notification_service.configuration.twillio.TwilioConfigurationProperties;
import com.kanwise.notification_service.model.sms.OtpSmsRequest;
import com.kanwise.notification_service.test.TestServiceInstanceListSupplier;
import com.twilio.Twilio;
import com.twilio.http.Request;
import com.twilio.http.Response;
import com.twilio.http.TwilioRestClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.boot.test.json.JsonContent;
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

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.kanwise.clients.user_service.authentication.model.OtpStatus.DELIVERED;
import static com.kanwise.clients.user_service.authentication.model.OtpStatus.FAILED;
import static com.kanwise.notification_service.listeners.KafkaTestingUtils.getKafkaProducerProperties;
import static com.kanwise.notification_service.model.kafka.TopicType.NOTIFICATION_SMS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.clients.admin.AdminClient.create;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.kafka.config.TopicBuilder.name;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@AutoConfigureWebTestClient
@DisplayName("Test sms message request listener")
@DirtiesContext
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class KafkaSmsListenersIT implements JsonTestingUtils {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
    private final AdminClient kafkaAdminClient;
    private final KafkaConfigurationProperties kafkaConfigurationProperties;
    private final KafkaProducer<String, OtpSmsRequest> kafkaOtpRequestProducer;
    private final BasicJsonTester json;
    private final TwilioConfigurationProperties twilioConfigurationProperties;
    private final java.net.http.HttpClient client;
    private final WireMockServer wireMockServer;
    private final ObjectMapper objectMapper;
    private MockWebServer mockWebServer;


    @Autowired
    public KafkaSmsListenersIT(KafkaAdmin kafkaAdmin, KafkaConfigurationProperties kafkaConfigurationProperties, KafkaProducer<String, OtpSmsRequest> kafkaOtpRequestProducer, TwilioConfigurationProperties twilioConfigurationProperties, WireMockServer wireMockServer, ObjectMapper objectMapper) {
        this.kafkaAdminClient = create(kafkaAdmin.getConfigurationProperties());
        this.kafkaConfigurationProperties = kafkaConfigurationProperties;
        this.kafkaOtpRequestProducer = kafkaOtpRequestProducer;
        this.twilioConfigurationProperties = twilioConfigurationProperties;
        this.wireMockServer = wireMockServer;
        this.objectMapper = objectMapper;
        this.client = HttpClient.newBuilder().build();
        this.json = new BasicJsonTester(this.getClass());
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        kafkaAdminClient.createTopics(List.of(name(kafkaConfigurationProperties.getTopicName(NOTIFICATION_SMS)).build()));
        mockWebServer = new MockWebServer();
        Twilio.setRestClient(getTestTwilioRestClient());
    }

    @AfterEach
    void afterEach() {
        wireMockServer.resetAll();
    }

    private TwilioRestClient getTestTwilioRestClient() {
        return new TwilioRestClient.Builder(twilioConfigurationProperties.accountSid(), twilioConfigurationProperties.authToken()).httpClient(
                new com.twilio.http.HttpClient() {
                    @Override
                    public Response makeRequest(Request request) {
                        try {
                            HttpResponse<String> httpResponse = client.sendAsync(constructHttpRequest(request), HttpResponse.BodyHandlers.ofString()).get();
                            return new Response(httpResponse.body(), httpResponse.statusCode());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    private HttpRequest constructHttpRequest(Request request) {
                        return HttpRequest.newBuilder()
                                .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                .uri(java.net.URI.create(mockWebServer.url("/").url().toString()))
                                .method(request.getMethod().name(), HttpRequest.BodyPublishers.ofString(generateJsonStringFromPostParams(request.getPostParams()), UTF_8))
                                .build();
                    }
                }
        ).build();
    }

    @Test
    void shouldConsumeOtmSmsRequestAndSendSms() throws InterruptedException {
        Thread.sleep(1000);
        // Given
        OtpSmsRequest otpSmsRequest = OtpSmsRequest.builder()
                .otpId(1L)
                .phoneNumber("15305431221")
                .content("Your OTP is 123456")
                .build();

        OtpSmsResponse otpSmsResponse = OtpSmsResponse.builder()
                .otpId(1L)
                .status(DELIVERED)
                .message("SMS_DELIVERED_SUCCESSFULLY")
                .build();
        // When
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .setBody(getJson("twilio_response/message-response.json")));

        wireMockServer.stubFor(post("/auth/otp/sms/response")
                .willReturn(ok()));

        kafkaOtpRequestProducer.send(new ProducerRecord<>(kafkaConfigurationProperties.getTopicName(NOTIFICATION_SMS), otpSmsRequest));
        // Then
        await().atMost(2, SECONDS).untilAsserted(() -> {
            RecordedRequest request = mockWebServer.takeRequest();
            JsonContent<Object> body = json.from(request.getBody().readUtf8());
            assertThat(body).extractingJsonPathStringValue("$.From").isEqualTo(twilioConfigurationProperties.number());
            assertThat(body).extractingJsonPathStringValue("$.To").isEqualTo(otpSmsRequest.getPhoneNumber());
            assertThat(body).extractingJsonPathStringValue("$.Body").isEqualTo(otpSmsRequest.getContent());
        });

        await().atMost(2, SECONDS).untilAsserted(() -> wireMockServer.verify(1, postRequestedFor(
                urlEqualTo("/auth/otp/sms/response")).withRequestBody(equalToJson(objectMapper.writeValueAsString(otpSmsResponse)))));
    }

    @Test
    void shouldConsumeOtmSmsRequestAndDoNotSendSms() throws Exception {
        Thread.sleep(1000);
        // Given
        OtpSmsRequest otpSmsRequest = OtpSmsRequest.builder()
                .otpId(1L)
                .phoneNumber("15305431221")
                .content("Your OTP is 123456")
                .build();

        OtpSmsResponse otpSmsResponse = OtpSmsResponse.builder()
                .otpId(1L)
                .status(FAILED)
                .message("An error occurred")
                .build();
        // When
        mockWebServer.enqueue(new MockResponse().setResponseCode(400)
                .setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .setBody(getJson("twilio_response/message-response-error.json")));

        wireMockServer.stubFor(post("/auth/otp/sms/response")
                .willReturn(ok()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(otpSmsResponse))));

        kafkaOtpRequestProducer.send(new ProducerRecord<>(kafkaConfigurationProperties.getTopicName(NOTIFICATION_SMS), otpSmsRequest));

        // Then
        await().atMost(2, SECONDS).untilAsserted(() -> {
            RecordedRequest request = mockWebServer.takeRequest();
            JsonContent<Object> body = json.from(request.getBody().readUtf8());
            assertThat(body).extractingJsonPathStringValue("$.From").isEqualTo(twilioConfigurationProperties.number());
            assertThat(body).extractingJsonPathStringValue("$.To").isEqualTo(otpSmsRequest.getPhoneNumber());
            assertThat(body).extractingJsonPathStringValue("$.Body").isEqualTo(otpSmsRequest.getContent());
        });

        await().atMost(2, SECONDS).untilAsserted(() -> wireMockServer.verify(1, postRequestedFor(
                urlEqualTo("/auth/otp/sms/response")).withRequestBody(equalToJson(objectMapper.writeValueAsString(otpSmsResponse)))));
    }

    private String getJson(String fileName) {
        return readJsonFileAsString(fileName, this.getClass().getClassLoader());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public KafkaProducer<String, OtpSmsRequest> kafkaOtpSmsRequestProducer() {
            return new KafkaProducer<>(getKafkaProducerProperties(kafkaContainer));
        }

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
}