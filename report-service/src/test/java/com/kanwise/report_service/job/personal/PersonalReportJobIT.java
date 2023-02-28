package com.kanwise.report_service.job.personal;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.kanwise.report_service.controller.DatabaseCleaner;
import com.kanwise.report_service.model.job_information.personal.PersonalReportJobInformation;
import com.kanwise.report_service.model.monitoring.personal.PersonalReportJobLog;
import com.kanwise.report_service.model.notification.email.EmailRequest;
import com.kanwise.report_service.service.job_information.monitoring.common.MonitoringService;
import com.kanwise.report_service.service.report_data.common.ReportDataService;
import liquibase.exception.LiquibaseException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.hibernate.AssertionFailure;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.kanwise.report_service.constant.job.JobConstant.ID;
import static com.kanwise.report_service.controller.kafka.KafkaTestingUtils.getKafkaConsumerProperties;
import static com.kanwise.report_service.model.monitoring.common.LogStatus.ERROR;
import static com.kanwise.report_service.model.monitoring.common.LogStatus.SUCCESS;
import static java.time.Duration.ofMillis;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.kafka.clients.admin.AdminClient.create;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.kafka.config.TopicBuilder.name;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static org.testcontainers.utility.DockerImageName.parse;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class PersonalReportJobIT {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(parse("confluentinc/cp-kafka:latest"));
    @Container
    static LocalStackContainer localStack = new LocalStackContainer(parse("localstack/localstack:0.13.0"))
            .withServices(S3);


    private final AmazonS3 amazonS3;
    private final DatabaseCleaner databaseCleaner;
    private final AdminClient kafkaAdminClient;
    private final Job personalReportJob;

    private final MonitoringService<PersonalReportJobLog, PersonalReportJobInformation> personalReportJobMonitoringService;

    @MockBean
    private ReportDataService<PersonalReportJobInformation> personalReportDataService;


    @Autowired
    PersonalReportJobIT(DatabaseCleaner databaseCleaner, KafkaAdmin kafkaAdmin, AmazonS3 amazonS3, Job personalReportJob, MonitoringService<PersonalReportJobLog, PersonalReportJobInformation> personalReportJobMonitoringService) {
        this.databaseCleaner = databaseCleaner;
        this.kafkaAdminClient = create(kafkaAdmin.getConfigurationProperties());
        this.amazonS3 = amazonS3;
        this.personalReportJob = personalReportJob;
        this.personalReportJobMonitoringService = personalReportJobMonitoringService;
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("digitalocean.spaces.accessKey", localStack::getAccessKey);
        registry.add("digitalocean.spaces.secretKey", localStack::getSecretKey);
        registry.add("digitalocean.spaces.signing-region", localStack::getRegion);
    }

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        localStack.execInContainer("awslocal", "s3", "mb", "s3://kanwise");
    }

    @BeforeEach
    void setUp() {
        when(personalReportDataService.getReportData(any())).thenReturn(getTestData());
        kafkaAdminClient.createTopics(singletonList(name("notification-email").build()));
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        kafkaAdminClient.deleteTopics(singletonList("notification-email"));
        kafkaAdminClient.close();
        databaseCleaner.setUp();
        reset(personalReportDataService);
    }

    @NotNull
    private Map<String, Object> getTestData() {
        return Map.of(
                "startDate", new int[]{2021, 1, 1, 0, 0, 0},
                "endDate", new int[]{2021, 1, 31, 23, 59, 59},
                "tasks", List.of(),
                "totalPerformance", 100,
                "totalEstimatedTime", 7200L,
                "totalActualTime", 7200L,
                "email", "kanwise@gmail.com",
                "firstName", "Test"
        );
    }

    @TestConfiguration
    static class AwsApiConfig {

        @Primary
        @Bean
        public AmazonS3 amazonS3() {
            return AmazonS3ClientBuilder
                    .standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localStack.getEndpointOverride(S3).toString(), localStack.getRegion()))
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(localStack.getAccessKey(), localStack.getSecretKey())))
                    .build();
        }
    }

    @Nested
    class ShouldHandlePersonalReportJobExecutionSuccess {

        @Test
        void shouldHandlePersonalReportJobSuccessExecutionAndCreateExecutionLog() throws Exception {
            // Given
            String id = "8d5d705e-6270-481b-b7bd-457fb3c49164";


            JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
            JobDetail jobDetail = mock(JobDetail.class);

            Map<String, Object> kafkaConsumerProperties = getKafkaConsumerProperties(kafkaContainer.getBootstrapServers());
            String topicName = "notification-email";
            // When
            when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
            when(jobDetail.getJobDataMap()).thenReturn(new JobDataMap(Map.of(ID, id)));

            Set<PersonalReportJobLog> logs = personalReportJobMonitoringService.getLogs(id);
            assertEquals(5, logs.size());
            // Then
            try (KafkaConsumer<String, EmailRequest> consumer = new KafkaConsumer<>(kafkaConsumerProperties)) {
                consumer.subscribe(singletonList(topicName));

                personalReportJob.execute(jobExecutionContext);

                AtomicReference<EmailRequest> emailRequest = new AtomicReference<>(EmailRequest.builder().build());

                await().atMost(5, SECONDS).until(() -> {
                    ConsumerRecords<String, EmailRequest> records = consumer.poll(ofMillis(100));
                    if (records.isEmpty()) {
                        return false;
                    }
                    assertThat(records.count(), is(1));
                    emailRequest.set(records.iterator().next().value());
                    return true;
                });

                Map<String, Object> emailRequestData = emailRequest.get().getData();
                String reportUrl = (String) emailRequestData.get("href");
                String reportKey = reportUrl.substring(reportUrl.indexOf("/reports/") + 1);
                await().until(() -> amazonS3.doesObjectExist("kanwise", reportKey));

                Set<PersonalReportJobLog> logsAfterExecution = personalReportJobMonitoringService.getLogs(id);
                assertEquals(6, logsAfterExecution.size());
                PersonalReportJobLog log = logsAfterExecution.stream().max(Comparator.comparing(PersonalReportJobLog::getTimestamp)).get();
                assertEquals(SUCCESS, log.getStatus());
            }
        }
    }

    @Nested
    class ShouldHandlePersonalReportJobExecutionFailure {

        @Test
        void shouldHandlePersonalReportJobFailureExecutionAndCreateExecutionFailureLog() throws Exception {
            // Given
            String id = "8d5d705e-6270-481b-b7bd-457fb3c49164";

            JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
            JobDetail jobDetail = mock(JobDetail.class);

            Map<String, Object> kafkaConsumerProperties = getKafkaConsumerProperties(kafkaContainer.getBootstrapServers());
            String topicName = "notification-email";

            // When
            when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
            when(jobDetail.getJobDataMap()).thenReturn(new JobDataMap(Map.of(ID, id)));
            when(personalReportDataService.getReportData(any())).thenThrow(new RuntimeException("Something went wrong"));

            Set<PersonalReportJobLog> logs = personalReportJobMonitoringService.getLogs(id);

            assertEquals(5, logs.size());

            // Then
            try (KafkaConsumer<String, EmailRequest> consumer = new KafkaConsumer<>(kafkaConsumerProperties)) {
                consumer.subscribe(singletonList(topicName));

                personalReportJob.execute(jobExecutionContext);


                long startTime = System.currentTimeMillis();

                while (System.currentTimeMillis() - startTime < 2000) {
                    ConsumerRecords<String, EmailRequest> records = consumer.poll(ofMillis(100));

                    if (records.isEmpty()) {
                        continue;
                    } else {
                        fail("Email request should not be sent");
                    }

                    amazonS3.listObjects("kanwise").getObjectSummaries().forEach(objectSummary -> {
                        if (objectSummary.getKey().startsWith("reports/")) {
                            fail("Report file should not be uploaded to S3");
                        }
                    });
                }

                Set<PersonalReportJobLog> logsAfterExecution = personalReportJobMonitoringService.getLogs(id);
                assertEquals(6, logsAfterExecution.size());
                PersonalReportJobLog log = logsAfterExecution.stream().max(Comparator.comparing(PersonalReportJobLog::getTimestamp)).orElseThrow(() -> new AssertionFailure("No log found"));
                assertEquals(ERROR, log.getStatus());
            }
        }
    }
}