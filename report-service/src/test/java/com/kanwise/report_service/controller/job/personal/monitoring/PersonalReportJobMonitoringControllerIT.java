package com.kanwise.report_service.controller.job.personal.monitoring;

import com.kanwise.report_service.controller.DatabaseCleaner;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.kanwise.report_service.constant.job.JobLogConstant.JOB_EXECUTION_SUCCESS_MESSAGE;
import static com.kanwise.report_service.constant.job.JobLogConstant.JOB_STOPPED_MESSAGE;
import static com.kanwise.report_service.model.http.HttpHeader.ROLE;
import static com.kanwise.report_service.model.http.HttpHeader.USERNAME;
import static com.kanwise.report_service.model.monitoring.common.LogStatus.CREATED;
import static com.kanwise.report_service.model.monitoring.common.LogStatus.SUCCESS;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class PersonalReportJobMonitoringControllerIT {

    private final MockMvc mockMvc;
    private final DatabaseCleaner databaseCleaner;

    @Autowired
    PersonalReportJobMonitoringControllerIT(MockMvc mockMvc, DatabaseCleaner databaseCleaner) {
        this.mockMvc = mockMvc;
        this.databaseCleaner = databaseCleaner;
    }

    @BeforeEach
    void setUp() throws LiquibaseException {
        databaseCleaner.setUp();
    }

    @Nested
    class ShouldGetPersonalReportJobLogs {

        @Test
        void shouldGetPersonalReportJobLogs() throws Exception {
            // Given
            String id = "8d5d705e-6270-481b-b7bd-457fb3c49164";
            // When
            // Then
            mockMvc.perform(get("/job/report/personal/{id}/logs", id)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(5)))
                    .andExpect(jsonPath("$[*].id").value(hasItems(1, 5, 9, 4, 8)))
                    .andExpect(jsonPath("$[*].subscriberUsername").value(hasItems("frneek", "frneek", "frneek", "frneek", "frneek")))
                    .andExpect(jsonPath("$[*].jobId").value(hasItems("8d5d705e-6270-481b-b7bd-457fb3c49164", "8d5d705e-6270-481b-b7bd-457fb3c49164", "8d5d705e-6270-481b-b7bd-457fb3c49164", "8d5d705e-6270-481b-b7bd-457fb3c49164", "8d5d705e-6270-481b-b7bd-457fb3c49164")))
                    .andExpect(jsonPath("$[*].status").value(hasItems(CREATED.name(), SUCCESS.name(), SUCCESS.name(), SUCCESS.name(), SUCCESS.name())))
                    .andExpect(jsonPath("$[*].timestamp").value(hasItems("2022-12-29T11:23:13.02885", "2022-12-29T11:25:00.61621", "2022-12-29T11:27:00.943295", "2022-12-29T11:24:01.489803", "2022-12-29T11:26:00.611087")))
                    .andExpect(jsonPath("$[*].message").value(hasItems(JOB_STOPPED_MESSAGE, JOB_EXECUTION_SUCCESS_MESSAGE, JOB_STOPPED_MESSAGE, JOB_EXECUTION_SUCCESS_MESSAGE, JOB_EXECUTION_SUCCESS_MESSAGE)))
                    .andExpect(jsonPath("$[*].links[0].rel").value(hasItems("subscriber", "subscriber", "subscriber", "subscriber", "subscriber")))
                    .andExpect(jsonPath("$[*].links[0].href").value(hasItems("http://localhost/subscriber/frneek", "http://localhost/subscriber/frneek", "http://localhost/subscriber/frneek", "http://localhost/subscriber/frneek", "http://localhost/subscriber/frneek")))
                    .andExpect(jsonPath("$[*].links[1].rel").value(hasItems("personal-job", "personal-job", "personal-job", "personal-job", "personal-job")))
                    .andExpect(jsonPath("$[*].links[1].href").value(hasItems("http://localhost/job/report/personal/8d5d705e-6270-481b-b7bd-457fb3c49164", "http://localhost/job/report/personal/8d5d705e-6270-481b-b7bd-457fb3c49164", "http://localhost/job/report/personal/8d5d705e-6270-481b-b7bd-457fb3c49164", "http://localhost/job/report/personal/8d5d705e-6270-481b-b7bd-457fb3c49164", "http://localhost/job/report/personal/8d5d705e-6270-481b-b7bd-457fb3c49164")))
                    .andExpect(jsonPath("$[*].data").value(notNullValue()))
                    .andExpect(jsonPath("$[*].data.reportUrl").value(hasItems("https://fra1.digitaloceanspaces.com/kanwise/reports/frneek/personal/frneek-personal-report-2022-12-29T11:25:00.134917.pdf",
                            "https://fra1.digitaloceanspaces.com/kanwise/reports/frneek/personal/frneek-personal-report-2022-12-29T11:24:00.210977.pdf", "https://fra1.digitaloceanspaces.com/kanwise/reports/frneek/personal/frneek-personal-report-2022-12-29T11:26:00.095098.pdf")))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotGetPersonalReportJobLogs {

        @Test
        void shouldNotGetPersonalReportJobLogsIfJobDoesNotExist() throws Exception {
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
            mockMvc.perform(get("/job/report/personal/{id}/logs", nonExistingJobId)
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
        void shouldNotGetPersonalReportJobLogsWithoutRoleHeader() throws Exception {
            // Given
            String jobId = "8d5d705e-6270-481b-b7bd-457fb3c49164";
            // When
            // Then
            mockMvc.perform(get("/job/report/personal/{id}/logs", jobId)
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetPersonalReportJobLogsWithoutUsernameHeader() throws Exception {
            // Given
            String jobId = "8d5d705e-6270-481b-b7bd-457fb3c49164";
            // When
            // Then
            mockMvc.perform(get("/job/report/personal/{id}/logs", jobId)
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