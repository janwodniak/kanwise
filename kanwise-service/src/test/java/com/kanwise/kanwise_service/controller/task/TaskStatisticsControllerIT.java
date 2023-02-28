package com.kanwise.kanwise_service.controller.task;

import com.kanwise.kanwise_service.controller.DatabaseCleaner;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.kanwise.kanwise_service.model.http.HttpHeader.ROLE;
import static com.kanwise.kanwise_service.model.http.HttpHeader.USERNAME;
import static java.time.ZonedDateTime.of;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class TaskStatisticsControllerIT {

    private static final ZonedDateTime NOW = of(
            2022, 12, 21, 14, 0, 0, 0, ZoneId.of("UTC")
    );

    private final MockMvc mockMvc;
    private final DatabaseCleaner databaseCleaner;
    @MockBean
    private Clock clock;

    @Autowired
    TaskStatisticsControllerIT(MockMvc mockMvc, DatabaseCleaner databaseCleaner) {
        this.mockMvc = mockMvc;
        this.databaseCleaner = databaseCleaner;
    }

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(clock.getZone()).thenReturn(NOW.getZone());
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }

    @Nested
    class ShouldFindTaskStatistics {

        @Test
        void shouldFindTaskStatistics() throws Exception {
            // Given
            Long taskId = 3L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}/statistics", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.projectId").value(1))
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.assignedMembersCount").value(1))
                    .andExpect(jsonPath("$.commentsCount").value(5))
                    .andExpect(jsonPath("$.statusesCount").value(3))
                    .andExpect(jsonPath("$.estimatedTime").value("PT3H"))
                    .andExpect(jsonPath("$.totalExistenceTime").value(startsWith("PT53H50M11S")))
                    .andExpect(jsonPath("$.taskStatusDurationMap.TODO").value(startsWith("PT31H16M39S")))
                    .andExpect(jsonPath("$.taskStatusDurationMap.IN_PROGRESS").value(startsWith("PT8H16M11S")))
                    .andExpect(jsonPath("$.taskStatusDurationMap.RESOLVED").value(startsWith("PT14H17M21S")))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.task.href").value("http://localhost/task/3"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotFindTaskStatistics {

        @Test
        void shouldNotFindTaskStatisticsIfTaskDoesNotExist() throws Exception {
            // Given
            Long taskId = 999L;
            // When
            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("TASK_NOT_FOUND"))
                    .andDo(print());
            // Then
            mockMvc.perform(get("/task/{id}/statistics", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("TASK_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotFindTaskStatisticsWithoutRoleHeader() throws Exception {
            // Given
            Long taskId = 3L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}/statistics", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotFindTaskStatisticsWithoutUsernameHeader() throws Exception {
            // Given
            Long taskId = 3L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}/statistics", taskId)
                            .contentType(APPLICATION_JSON)
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