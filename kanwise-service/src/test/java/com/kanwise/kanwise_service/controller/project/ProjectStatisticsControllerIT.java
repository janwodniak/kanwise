package com.kanwise.kanwise_service.controller.project;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static com.kanwise.kanwise_service.model.http.HttpHeader.ROLE;
import static com.kanwise.kanwise_service.model.http.HttpHeader.USERNAME;
import static java.time.ZonedDateTime.of;
import static org.hamcrest.Matchers.hasItems;
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
class ProjectStatisticsControllerIT {


    private static final ZonedDateTime NOW = of(
            2022, 12, 21, 14, 0, 0, 0, ZoneId.of("UTC")
    );

    private final MockMvc mockMvc;
    private final DatabaseCleaner databaseCleaner;
    @MockBean
    private Clock clock;

    @Autowired
    ProjectStatisticsControllerIT(MockMvc mockMvc, DatabaseCleaner databaseCleaner) {
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
    class ShouldGetProjectStatistics {

        @Test
        void shouldGetProjectStatistics() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/statistics", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.projectId").value(projectId))
                    .andExpect(jsonPath("$.totalTasksCount").value(6))
                    .andExpect(jsonPath("$.performancePercentage").value(42))
                    .andExpect(jsonPath("$.totalEstimatedTime").value("PT61H"))
                    .andExpect(jsonPath("$.totalTasksStatusCountMap.TODO").value(3))
                    .andExpect(jsonPath("$.totalTasksStatusCountMap.IN_PROGRESS").value(1))
                    .andExpect(jsonPath("$.totalTasksStatusCountMap.RESOLVED").value(2))
                    .andExpect(jsonPath("$.totalTasksStatusDurationMap.TODO").value("PT253H5M54S"))
                    .andExpect(jsonPath("$.totalTasksStatusDurationMap.IN_PROGRESS").value("PT145H20M"))
                    .andExpect(jsonPath("$.totalTasksStatusDurationMap.RESOLVED").value(startsWith("PT30H57M22S")))
                    .andExpect(jsonPath("$.totalTasksTypeCountMap.TEST").value(1))
                    .andExpect(jsonPath("$.totalTasksTypeCountMap.NEW_FEATURE").value(3))
                    .andExpect(jsonPath("$.totalTasksTypeCountMap.BUG").value(1))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotGetProjectStatistics {

        @Test
        void shouldNotGetProjectStatisticsIfProjectDoesNotExist() throws Exception {
            // Given
            Long projectId = 100L;
            // When
            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
            // Then
            mockMvc.perform(get("/project/{id}/statistics", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetProjectStatisticsWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/statistics", projectId)
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
        void shouldNotGetProjectStatisticsWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/statistics", projectId)
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

    @Nested
    class ShouldGetProjectStatisticsForMember {

        @Test
        void shouldGetProjectStatisticsForMember() throws Exception {
            // Given
            Long projectId = 1L;
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(get("/project/{id}/statistics", projectId)
                            .param(USERNAME, username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.projectId").value(projectId))
                    .andExpect(jsonPath("$.totalTasksCount").value(5))
                    .andExpect(jsonPath("$.performancePercentage").value(44))
                    .andExpect(jsonPath("$.totalEstimatedTime").value(startsWith("PT57H")))
                    .andExpect(jsonPath("$.totalTasksStatusCountMap.TODO").value(3))
                    .andExpect(jsonPath("$.totalTasksStatusCountMap.IN_PROGRESS").value(1))
                    .andExpect(jsonPath("$.totalTasksStatusCountMap.RESOLVED").value(1))
                    .andExpect(jsonPath("$.totalTasksStatusDurationMap.TODO").value(startsWith("PT227H33M53S")))
                    .andExpect(jsonPath("$.totalTasksStatusDurationMap.IN_PROGRESS").value(startsWith("PT128H16M41S")))
                    .andExpect(jsonPath("$.totalTasksStatusDurationMap.RESOLVED").value(startsWith("PT14H17M21S")))
                    .andExpect(jsonPath("$.totalTasksTypeCountMap.BUG").value(1))
                    .andExpect(jsonPath("$.totalTasksTypeCountMap.TEST").value(1))
                    .andExpect(jsonPath("$.totalTasksTypeCountMap.NEW_FEATURE").value(3))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andDo(print());
        }

    }

    @Nested
    class ShouldNotGetProjectStatisticsForMember {

        @Test
        void shouldNotGetProjectStatisticsForMemberIfProjectDoesNotExist() throws Exception {
            // Given
            Long projectId = 100L;
            // When
            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
            // Then
            mockMvc.perform(get("/project/{id}/statistics", projectId)
                            .contentType(APPLICATION_JSON)
                            .param(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetProjectStatisticsForMemberIfMemberDoesNotExist() throws Exception {
            // Given
            Long projectId = 1L;
            String nonExistingUsername = "nonExistingUsername";
            // When
            mockMvc.perform(get("/member/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
            // Then
            mockMvc.perform(get("/project/{id}/statistics", projectId)
                            .param(USERNAME, nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetProjectStatisticsForMemberWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/statistics", projectId)
                            .param(USERNAME, "frneek")
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
        void shouldNotGetProjectStatisticsForMemberWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/statistics", projectId)
                            .param(USERNAME, "frneek")
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

    @Nested
    class ShouldGetTasksStatisticsOfProject {

        @Test
        void shouldGetTasksStatisticsOfProject() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/tasks/statistics", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[*].projectId").value(hasItems(1, 1, 1, 1, 1)))
                    .andExpect(jsonPath("$[*].taskId").value(hasItems(1, 2, 4, 5, 3)))
                    .andExpect(jsonPath("$[*].assignedMembersCount").value(hasItems(2, 1, 2, 2, 1)))
                    .andExpect(jsonPath("$[*].commentsCount").value(hasItems(0, 0, 0, 0, 5)))
                    .andExpect(jsonPath("$[*].statusesCount").value(hasItems(1, 2, 1, 1, 3)))
                    .andExpect(jsonPath("[*].estimatedTime").value(hasItems(10800.000000000, 64800.000000000, 21600.000000000, 21600.000000000, 86400.000000000)))
                    .andExpect(jsonPath("[*].totalExistenceTime").value(hasItems(193811.000000000, 518400.000000000, 83333.000000000, 345688.000000000, 191243.000000000)))
                    .andExpect(jsonPath("[*].taskStatusDurationMap").value(hasItems(
                            Map.of("TODO", 112599.000000000, "IN_PROGRESS", 29771.000000000, "RESOLVED", 51441.000000000),
                            Map.of("TODO", 86370.000000000, "IN_PROGRESS", 432030.000000000, "RESOLVED", 0.0),
                            Map.of("TODO", 83333.000000000, "IN_PROGRESS", 0.0, "RESOLVED", 0.0),
                            Map.of("TODO", 345688.000000000, "IN_PROGRESS", 0.0, "RESOLVED", 0.0),
                            Map.of("TODO", 191243.000000000, "IN_PROGRESS", 0.0, "RESOLVED", 0.0)
                    )))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/project/1",
                            "http://localhost/task/1",
                            "http://localhost/project/1",
                            "http://localhost/task/2",
                            "http://localhost/project/1",
                            "http://localhost/task/4",
                            "http://localhost/project/1",
                            "http://localhost/task/5",
                            "http://localhost/project/1",
                            "http://localhost/task/3"
                    ))).andDo(print());
        }

    }

    @Nested
    class ShouldNotGetTasksStatisticsOfProject {

        @Test
        void shouldNotGetTasksStatisticsOfProjectIfProjectDoesNotExist() throws Exception {
            // Given
            Long nonExistingProjectId = 100L;
            // When
            mockMvc.perform(get("/project/{id}", nonExistingProjectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
            // Then
            mockMvc.perform(get("/project/{id}/tasks/statistics", nonExistingProjectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetTasksStatisticsOfProjectWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/tasks/statistics", projectId)
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
        void shouldNotGetTasksStatisticsOfProjectWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/tasks/statistics", projectId)
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