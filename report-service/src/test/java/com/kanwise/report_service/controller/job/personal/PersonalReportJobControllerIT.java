package com.kanwise.report_service.controller.job.personal;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.report_service.controller.DatabaseCleaner;
import com.kanwise.report_service.model.job_information.personal.dto.PersonalReportJobInformationDto;
import com.kanwise.report_service.model.job_information.personal.request.PersonalReportJobRequest;
import liquibase.exception.LiquibaseException;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.kanwise.report_service.constant.job.JobConstant.ID;
import static com.kanwise.report_service.model.http.HttpHeader.ROLE;
import static com.kanwise.report_service.model.http.HttpHeader.USERNAME;
import static com.kanwise.report_service.model.report.JobGroup.PERSONAL_REPORT;
import static java.time.ZonedDateTime.of;
import static java.util.Collections.singletonList;
import static org.apache.kafka.clients.admin.AdminClient.create;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quartz.JobKey.jobKey;
import static org.quartz.impl.matchers.GroupMatcher.groupStartsWith;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.kafka.config.TopicBuilder.name;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.utility.DockerImageName.parse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class PersonalReportJobControllerIT {

    private static final ZonedDateTime NOW = of(
            2022, 12, 21, 14, 0, 0, 0, ZoneId.of("UTC")
    );
    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(parse("confluentinc/cp-kafka:latest"));
    @Container
    static LocalStackContainer localStack = new LocalStackContainer(parse("localstack/localstack:0.13.0"))
            .withServices(S3);
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DatabaseCleaner databaseCleaner;
    private final AdminClient kafkaAdminClient;
    private final Scheduler scheduler;


    @Autowired
    PersonalReportJobControllerIT(MockMvc mockMvc, ObjectMapper objectMapper, DatabaseCleaner databaseCleaner, KafkaAdmin kafkaAdmin, Scheduler scheduler) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.databaseCleaner = databaseCleaner;
        this.kafkaAdminClient = create(kafkaAdmin.getConfigurationProperties());
        this.scheduler = scheduler;
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
    void setUp() throws LiquibaseException {
        databaseCleaner.setUp();
        kafkaAdminClient.createTopics(singletonList(name("notification-email").build()));
    }

    @AfterEach
    void tearDown() {
        kafkaAdminClient.deleteTopics(singletonList("notification-email"));
        kafkaAdminClient.close();
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
    class ShouldRunPersonalReportJob {

        @Test
        void shouldRunPersonalReportCronJob() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .cron("0 0 0 1/1 * ? *")
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .build();
            // When
            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            String responseJson = mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.cron").value("0 0 0 1/1 * ? *"))
                    .andExpect(jsonPath("$.description").value("at 00:00 every day"))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.subscriberUsername").value("frneek"))
                    .andExpect(jsonPath("$.startDate").value("2022-12-11T14:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2022-12-31T14:00:00"))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.subscriber.href").value("http://localhost/subscriber/frneek"))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            PersonalReportJobInformationDto personalJobInformationDto = objectMapper.readValue(responseJson, PersonalReportJobInformationDto.class);
            JobKey jobKey = jobKey(personalJobInformationDto.getId(), PERSONAL_REPORT.name());
            JobDataMap jobDataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
            List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);

            assertNotNull(jobDataMap);
            assertTrue(jobDataMap.containsKey(ID));
            assertEquals(personalJobInformationDto.getId(), jobDataMap.get(ID));
            assertEquals(1, triggersOfJob.size());
            assertNull(triggersOfJob.get(0).getCalendarName());
            assertEquals(0, triggersOfJob.get(0).getMisfireInstruction());
            assertEquals("org.quartz.impl.triggers.CronTriggerImpl", triggersOfJob.get(0).getClass().getName());
            assertEquals(3, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }


        @Test
        void shouldRunPersonalReportNonCronJob() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .totalFireCount(10)
                    .runForever(false)
                    .repeatInterval(1000000000)
                    .initialOffsetMs(1000000000)
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .build();

            // When
            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            String responseJson = mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.totalFireCount").value(10))
                    .andExpect(jsonPath("$.remainingFireCount").value(10))
                    .andExpect(jsonPath("$.runForever").value(false))
                    .andExpect(jsonPath("$.repeatInterval").value(1000000000))
                    .andExpect(jsonPath("$.initialOffsetMs").value(1000000000))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.subscriberUsername").value("frneek"))
                    .andExpect(jsonPath("$.startDate").value("2022-12-11T14:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2022-12-31T14:00:00"))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.subscriber.href").value("http://localhost/subscriber/frneek"))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            PersonalReportJobInformationDto personalJobInformationDto = objectMapper.readValue(responseJson, PersonalReportJobInformationDto.class);
            JobKey jobKey = jobKey(personalJobInformationDto.getId(), PERSONAL_REPORT.name());
            JobDataMap jobDataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
            List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);

            assertNotNull(jobDataMap);
            assertTrue(jobDataMap.containsKey(ID));
            assertEquals(personalJobInformationDto.getId(), jobDataMap.get(ID));
            assertEquals(1, triggersOfJob.size());
            assertNull(triggersOfJob.get(0).getCalendarName());
            assertEquals(0, triggersOfJob.get(0).getMisfireInstruction());
            assertEquals("org.quartz.impl.triggers.SimpleTriggerImpl", triggersOfJob.get(0).getClass().getName());
            assertEquals(3, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }

        @Test
        void shouldPersonalReportJobWithForeverTrigger() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .runForever(true)
                    .repeatInterval(1000000000)
                    .initialOffsetMs(1000000000)
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .build();
            // When
            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            String responseJson = mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.runForever").value(true))
                    .andExpect(jsonPath("$.repeatInterval").value(1000000000))
                    .andExpect(jsonPath("$.initialOffsetMs").value(1000000000))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.subscriberUsername").value("frneek"))
                    .andExpect(jsonPath("$.startDate").value("2022-12-11T14:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2022-12-31T14:00:00"))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.subscriber.href").value("http://localhost/subscriber/frneek"))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            PersonalReportJobInformationDto personalJobInformationDto = objectMapper.readValue(responseJson, PersonalReportJobInformationDto.class);
            JobKey jobKey = jobKey(personalJobInformationDto.getId(), PERSONAL_REPORT.name());
            JobDataMap jobDataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
            List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);

            assertNotNull(jobDataMap);
            assertTrue(jobDataMap.containsKey(ID));
            assertEquals(personalJobInformationDto.getId(), jobDataMap.get(ID));
            assertEquals(1, triggersOfJob.size());
            assertNull(triggersOfJob.get(0).getCalendarName());
            assertEquals(0, triggersOfJob.get(0).getMisfireInstruction());
            assertEquals("org.quartz.impl.triggers.SimpleTriggerImpl", triggersOfJob.get(0).getClass().getName());
            assertEquals(3, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }
    }

    @Nested
    class ShouldNotRunPersonalReportJob {

        @Test
        void shouldNotRunPersonalReportJobWithNullName() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name(null)
                    .cron("0 0 0 1/1 * ? *")
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .build();
            // When
            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'name' && @.message == 'JOB_NAME_NOT_BLANK')]").exists())
                    .andDo(print());

            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }

        @Test
        void shouldNotRunPersonalReportJobWithNegativeFireCount() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .cron("0 0 0 1/1 * ? *")
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .totalFireCount(-1)
                    .build();
            // When
            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'totalFireCount' && @.message == 'TOTAL_FIRE_COUNT_NOT_NEGATIVE')]").exists())
                    .andDo(print());

            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }

        @Test
        void shouldNotRunPersonalReportJobWithNegativeRepeatInterval() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .cron("0 0 0 1/1 * ? *")
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .repeatInterval(-1)
                    .build();
            // When
            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'repeatInterval' && @.message == 'REPEAT_INTERVAL_NOT_NEGATIVE')]").exists())
                    .andDo(print());

            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }

        @Test
        void shouldNotRunPersonalReportJobWithNegativeInitialOffsetMs() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .cron("0 0 0 1/1 * ? *")
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .initialOffsetMs(-1)
                    .build();
            // When
            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'initialOffsetMs' && @.message == 'INITIAL_OFFSET_MS_NOT_NEGATIVE')]").exists())
                    .andDo(print());

            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }


        @Test
        void shouldNotRunPersonalReportJobWithInvalidCron() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .cron("0 0 0 1/1 * ? a")
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .build();
            // When
            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'cron' && @.message == 'INVALID_CRON_PATTERN')]").exists())
                    .andDo(print());

            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }

        @Test
        void shouldNotRunPersonalReportJobWithNullStartDate() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .cron("0 0 0 1/1 * ? *")
                    .username("frneek")
                    .startDate(null)
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .build();
            // When
            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'startDate' && @.message == 'START_DATE_NOT_NULL')]").exists())
                    .andDo(print());

            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }

        @Test
        void shouldNotRunPersonalReportJobWithNullEndDate() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .cron("0 0 0 1/1 * ? *")
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(null)
                    .build();
            // When
            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'endDate' && @.message == 'END_DATE_NOT_NULL')]").exists())
                    .andDo(print());

            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }

        @Test
        void shouldNotRunPersonalReportJobWithoutRoleHeader() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .cron("0 0 0 1/1 * ? *")
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .build();
            // When
            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());

            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }

        @Test
        void shouldNotRunPersonalReportJobWithoutUsernameHeader() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .cron("0 0 0 1/1 * ? *")
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .build();
            // When
            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());

            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }
    }

    @Nested
    class ShouldGetPersonalReportJob {

        @Test
        void shouldGetPersonalReportJob() throws Exception {
            // Given
            String id = "8d5d705e-6270-481b-b7bd-457fb3c49164";
            // When
            // Then
            mockMvc.perform(get("/job/report/personal/{id}", id)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.subscriberUsername").value("frneek"))
                    .andExpect(jsonPath("$.startDate").value("2022-12-11T23:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2022-12-30T23:00:00"))
                    .andExpect(jsonPath("$.status").value("STOPPED"))
                    .andExpect(jsonPath("$._links.subscriber.href").value("http://localhost/subscriber/frneek"))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.job-logs.href").value("http://localhost/job/report/personal/%s/logs".formatted(id)))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotGetPersonalReportJob {

        @Test
        void shouldNotGetPersonalReportJobIfJobDoesNotExist() throws Exception {
            // Given
            String nonExistingJobId = "8d5d705e-6270-481b-b7bd-457fb3c49165";
            // When
            // Then
            mockMvc.perform(get("/job/report/personal/{id}", nonExistingJobId)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("JOB_WITH_ID_%s_NOT_FOUND".formatted(nonExistingJobId)))
                    .andDo(print());
        }

        @Test
        void shouldNotGetPersonalReportJobWithoutRoleHeader() throws Exception {
            // Given
            String id = "8d5d705e-6270-481b-b7bd-457fb3c49164";
            // When
            // Then
            mockMvc.perform(get("/job/report/personal/{id}", id)
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetPersonalReportJobWithoutUsernameHeader() throws Exception {
            // Given
            String id = "8d5d705e-6270-481b-b7bd-457fb3c49164";
            // When
            // Then
            mockMvc.perform(get("/job/report/personal/{id}", id)
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldGetPersonalReportJobs {

        @Test
        void shouldGetAllPersonalReportJobs() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/job/report/personal")
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].id").value(hasItems("8d5d705e-6270-481b-b7bd-457fb3c49164", "ecae2660-94ef-4636-8c7b-f1cab90f29d8")))
                    .andExpect(jsonPath("$[*].cron").value(hasItems("0 0/1 * * * ?", "0 0/1 * * * ?")))
                    .andExpect(jsonPath("$[*].description").value(hasItems("every minute", "every minute")))
                    .andExpect(jsonPath("$[*].startDate").value(hasItems("2022-12-11T23:00:00", "2022-12-04T23:00:00")))
                    .andExpect(jsonPath("$[*].endDate").value(hasItems("2022-12-30T23:00:00", "2023-04-28T22:00:00")))
                    .andExpect(jsonPath("$[*].status").value(hasItems("STOPPED", "STOPPED")))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("subscriber", "job-logs", "personal-reports", "project-reports")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems("http://localhost/subscriber/frneek", "http://localhost/job/report/personal/8d5d705e-6270-481b-b7bd-457fb3c49164/logs", "http://localhost/subscriber/frneek/reports/personal{?status}", "http://localhost/subscriber/frneek/reports/project{?status}")))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotGetPersonalReportJobs {

        @Test
        void shouldNotGetPersonalReportJobsWithoutRoleHeader() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/job/report/personal")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetPersonalReportJobsWithoutUsernameHeader() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/job/report/personal")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldStopPersonalReportJob {

        @Test
        void shouldStopPersonalReportJob() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .cron("0 0 0 1/1 * ? *")
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .build();
            // When
            String responseJson = mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.cron").value("0 0 0 1/1 * ? *"))
                    .andExpect(jsonPath("$.description").value("at 00:00 every day"))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.subscriberUsername").value("frneek"))
                    .andExpect(jsonPath("$.startDate").value("2022-12-11T14:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2022-12-31T14:00:00"))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.subscriber.href").value("http://localhost/subscriber/frneek"))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            PersonalReportJobInformationDto personalJobInformationDto = objectMapper.readValue(responseJson, PersonalReportJobInformationDto.class);
            JobKey jobKey = jobKey(personalJobInformationDto.getId(), PERSONAL_REPORT.name());
            assertEquals(3, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            CronTriggerImpl trigger = (CronTriggerImpl) scheduler.getTriggersOfJob(jobKey).get(0);
            Assertions.assertEquals(Trigger.TriggerState.NORMAL, scheduler.getTriggerState(trigger.getKey()));
            // Then

            mockMvc.perform(post("/job/report/personal/{id}/stop", personalJobInformationDto.getId())
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cron").value("0 0 0 1/1 * ? *"))
                    .andExpect(jsonPath("$.description").value("at 00:00 every day"))
                    .andExpect(jsonPath("$.id").value(personalJobInformationDto.getId()))
                    .andExpect(jsonPath("$.subscriberUsername").value("frneek"))
                    .andExpect(jsonPath("$.startDate").value("2022-12-11T14:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2022-12-31T14:00:00"))
                    .andExpect(jsonPath("$.status").value("STOPPED"))
                    .andExpect(jsonPath("$._links.subscriber.href").value("http://localhost/subscriber/frneek"))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());

            CronTriggerImpl stoppedTrigger = (CronTriggerImpl) scheduler.getTriggersOfJob(jobKey).get(0);
            assertEquals(3, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            Assertions.assertEquals(Trigger.TriggerState.PAUSED, scheduler.getTriggerState(stoppedTrigger.getKey()));
        }
    }

    @Nested
    class ShouldNotStopPersonalReportJob {

        @Test
        void shouldNotStopPersonalReportJobIfJobDoesNotExist() throws Exception {
            String nonExistingJobId = "8d5d705e-6270-481b-b7bd-457fb3c49165";
            // When
            // Then
            mockMvc.perform(post("/job/report/personal/{id}/stop", nonExistingJobId)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("JOB_WITH_ID_%s_NOT_FOUND".formatted(nonExistingJobId)))
                    .andDo(print());

        }

        @Test
        void shouldNotStopPersonalReportJobWithoutRoleHeader() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .cron("0 0 0 1/1 * ? *")
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .build();
            // When
            String responseJson = mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.cron").value("0 0 0 1/1 * ? *"))
                    .andExpect(jsonPath("$.description").value("at 00:00 every day"))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.subscriberUsername").value("frneek"))
                    .andExpect(jsonPath("$.startDate").value("2022-12-11T14:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2022-12-31T14:00:00"))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.subscriber.href").value("http://localhost/subscriber/frneek"))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            PersonalReportJobInformationDto personalJobInformationDto = objectMapper.readValue(responseJson, PersonalReportJobInformationDto.class);
            JobKey jobKey = jobKey(personalJobInformationDto.getId(), PERSONAL_REPORT.name());
            assertEquals(3, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            CronTriggerImpl trigger = (CronTriggerImpl) scheduler.getTriggersOfJob(jobKey).get(0);
            Assertions.assertEquals(Trigger.TriggerState.NORMAL, scheduler.getTriggerState(trigger.getKey()));
            // Then
            mockMvc.perform(post("/job/report/personal/{id}/stop", personalJobInformationDto.getId())
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());

            CronTriggerImpl stoppedTrigger = (CronTriggerImpl) scheduler.getTriggersOfJob(jobKey).get(0);
            assertEquals(3, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            Assertions.assertEquals(Trigger.TriggerState.NORMAL, scheduler.getTriggerState(stoppedTrigger.getKey()));
        }

        @Test
        void shouldNotStopPersonalReportJobWithoutUsernameHeader() throws Exception {
            // Given
            PersonalReportJobRequest personalReportJob = PersonalReportJobRequest.builder()
                    .name("Personal Report Job")
                    .cron("0 0 0 1/1 * ? *")
                    .username("frneek")
                    .startDate(NOW.toLocalDateTime().minus(10, ChronoUnit.DAYS))
                    .endDate(NOW.toLocalDateTime().plus(10, ChronoUnit.DAYS))
                    .build();
            // When
            String responseJson = mockMvc.perform(post("/job/report/personal")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(personalReportJob)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.cron").value("0 0 0 1/1 * ? *"))
                    .andExpect(jsonPath("$.description").value("at 00:00 every day"))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.subscriberUsername").value("frneek"))
                    .andExpect(jsonPath("$.startDate").value("2022-12-11T14:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2022-12-31T14:00:00"))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.subscriber.href").value("http://localhost/subscriber/frneek"))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            PersonalReportJobInformationDto personalJobInformationDto = objectMapper.readValue(responseJson, PersonalReportJobInformationDto.class);
            JobKey jobKey = jobKey(personalJobInformationDto.getId(), PERSONAL_REPORT.name());
            assertEquals(3, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            CronTriggerImpl trigger = (CronTriggerImpl) scheduler.getTriggersOfJob(jobKey).get(0);
            Assertions.assertEquals(Trigger.TriggerState.NORMAL, scheduler.getTriggerState(trigger.getKey()));

            // Then
            mockMvc.perform(post("/job/report/personal/{id}/stop", personalJobInformationDto.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());

            CronTriggerImpl stoppedTrigger = (CronTriggerImpl) scheduler.getTriggersOfJob(jobKey).get(0);
            assertEquals(3, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            Assertions.assertEquals(Trigger.TriggerState.NORMAL, scheduler.getTriggerState(stoppedTrigger.getKey()));
        }
    }

    @Nested
    class ShouldRestartPersonalReportJob {

        @Test
        void shouldRestartPersonalReportJob() throws Exception {
            // Given
            String id = "8d5d705e-6270-481b-b7bd-457fb3c49164";
            // When
            mockMvc.perform(get("/job/report/personal/{id}", id)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.subscriberUsername").value("frneek"))
                    .andExpect(jsonPath("$.startDate").value("2022-12-11T23:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2022-12-30T23:00:00"))
                    .andExpect(jsonPath("$.status").value("STOPPED"))
                    .andExpect(jsonPath("$._links.subscriber.href").value("http://localhost/subscriber/frneek"))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.job-logs.href").value("http://localhost/job/report/personal/%s/logs".formatted(id)))
                    .andDo(print());

            JobKey jobKey = jobKey(id, PERSONAL_REPORT.name());
            CronTriggerImpl trigger = (CronTriggerImpl) scheduler.getTriggersOfJob(jobKey).get(0);
            Assertions.assertEquals(Trigger.TriggerState.PAUSED, scheduler.getTriggerState(trigger.getKey()));
            // Then
            mockMvc.perform(post("/job/report/personal/{id}/restart", id)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.subscriberUsername").value("frneek"))
                    .andExpect(jsonPath("$.startDate").value("2022-12-11T23:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2022-12-30T23:00:00"))
                    .andExpect(jsonPath("$.status").value("RESTARTED"))
                    .andExpect(jsonPath("$._links.subscriber.href").value("http://localhost/subscriber/frneek"))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.job-logs.href").value("http://localhost/job/report/personal/%s/logs".formatted(id)))
                    .andDo(print());

            CronTriggerImpl restartedTrigger = (CronTriggerImpl) scheduler.getTriggersOfJob(jobKey).get(0);
            Assertions.assertEquals(Trigger.TriggerState.NORMAL, scheduler.getTriggerState(restartedTrigger.getKey()));
        }
    }

    @Nested
    class ShouldNotRestartPersonalReportJob {

        @Test
        void shouldNotRestartPersonalReportJobIfJobDoesNotExist() throws Exception {
            // Given
            String nonExistingJobId = "8d5d705e-6270-481b-b7bd-457fb3c49165";
            // When
            // Then
            mockMvc.perform(post("/job/report/personal/{id}/restart", nonExistingJobId)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("JOB_WITH_ID_%s_NOT_FOUND".formatted(nonExistingJobId)))
                    .andDo(print());
        }

        @Test
        void shouldNotRestartPersonalReportJobWithoutRoleHeader() throws Exception {
            // Given
            String id = "8d5d705e-6270-481b-b7bd-457fb3c49165";
            // When
            // Then
            mockMvc.perform(post("/job/report/personal/{id}/restart", id)
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotRestartPersonalReportJobWithoutUsernameHeader() throws Exception {
            // Given
            String id = "8d5d705e-6270-481b-b7bd-457fb3c49165";
            // When
            // Then
            mockMvc.perform(post("/job/report/personal/{id}/restart", id)
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldDeletePersonalReportJob {

        @Test
        void shouldDeletePersonalReportJob() throws Exception {
            // Given
            String id = "8d5d705e-6270-481b-b7bd-457fb3c49164";
            // When
            mockMvc.perform(get("/job/report/personal/{id}", id)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.subscriberUsername").value("frneek"))
                    .andExpect(jsonPath("$.startDate").value("2022-12-11T23:00:00"))
                    .andExpect(jsonPath("$.endDate").value("2022-12-30T23:00:00"))
                    .andExpect(jsonPath("$.status").value("STOPPED"))
                    .andExpect(jsonPath("$._links.subscriber.href").value("http://localhost/subscriber/frneek"))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.job-logs.href").value("http://localhost/job/report/personal/%s/logs".formatted(id)))
                    .andDo(print());

            assertEquals(2, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
            // Then
            mockMvc.perform(delete("/job/report/personal/{id}", id)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            mockMvc.perform(get("/job/report/personal/{id}", id)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("JOB_WITH_ID_%s_NOT_FOUND".formatted(id)))
                    .andDo(print());

            assertEquals(1, scheduler.getJobKeys(groupStartsWith(PERSONAL_REPORT.name())).size());
        }
    }

    @Nested
    class ShouldNotDeletePersonalReportJob {

        @Test
        void shouldNotDeletePersonalReportJobIfJobDoesNotExist() throws Exception {
            // Given
            String nonExistingJobId = "8d5d705e-6270-481b-b7bd-457fb3c49165";
            // When
            mockMvc.perform(get("/job/report/personal/{id}", nonExistingJobId)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("JOB_WITH_ID_%s_NOT_FOUND".formatted(nonExistingJobId)))
                    .andDo(print());
            // Then
            mockMvc.perform(delete("/job/report/personal/{id}", nonExistingJobId)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("JOB_WITH_ID_%s_NOT_FOUND".formatted(nonExistingJobId)))
                    .andDo(print());
        }

        @Test
        void shouldNotDeletePersonalReportJobWithoutRoleHeader() throws Exception {
            // Given
            String id = "8d5d705e-6270-481b-b7bd-457fb3c49164";
            // When
            // Then
            mockMvc.perform(delete("/job/report/personal/{id}", id)
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotDeletePersonalReportJobWithoutUsernameHeader() throws Exception {
            // Given
            String id = "8d5d705e-6270-481b-b7bd-457fb3c49164";
            // When
            // Then
            mockMvc.perform(delete("/job/report/personal/{id}", id)
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }
}