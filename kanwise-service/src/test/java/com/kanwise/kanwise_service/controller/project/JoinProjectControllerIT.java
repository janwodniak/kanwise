package com.kanwise.kanwise_service.controller.project;

import com.kanwise.kanwise_service.controller.DatabaseCleaner;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.kanwise.kanwise_service.model.http.HttpHeader.ROLE;
import static com.kanwise.kanwise_service.model.http.HttpHeader.USERNAME;
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
class JoinProjectControllerIT {

    private final MockMvc mockMvc;
    private final DatabaseCleaner databaseCleaner;

    @Autowired
    JoinProjectControllerIT(MockMvc mockMvc, DatabaseCleaner databaseCleaner) {
        this.mockMvc = mockMvc;
        this.databaseCleaner = databaseCleaner;
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }

    @Nested
    class ShouldGetJoinRequestsForProject {

        @Test
        void shouldGetJoinRequestsForProject() throws Exception {
            // Given
            Long projectId = 3L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/join/requests", projectId)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.[0].id").value(3))
                    .andExpect(jsonPath("$.[0].projectId").value(projectId))
                    .andExpect(jsonPath("$.[0].requestedAt").exists())
                    .andExpect(jsonPath("$.[0].message").value("Hi, I am a devops engineer interested in joining the Kanwise-Devops project. I have experience with CI/CD pipelines and automating builds and deployments. Please let me know if you have any openings or if you'd like to discuss further."))
                    .andExpect(jsonPath("$.[0].requestedByUsername").value("jaroslawPsikuta"))
                    .andExpect(jsonPath("$.[0].links.[0].rel").value("requested-by"))
                    .andExpect(jsonPath("$.[0].links.[0].href").value("http://localhost/member/jaroslawPsikuta"))
                    .andExpect(jsonPath("$.[0].links.[1].rel").value("project"))
                    .andExpect(jsonPath("$.[0].links.[1].href").value("http://localhost/project/3"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotGetJoinRequestsForProject {

        @Test
        void shouldNotGetJoinRequestsForProjectIfProjectDoesNotExist() throws Exception {
            // Given
            Long projectId = 999L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/join/requests", projectId)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetJoinRequestsForProjectWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 3L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/join/requests", projectId)
                            .header(USERNAME, "frneek")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetJoinRequestsForProjectWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 3L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/join/requests", projectId)
                            .header(ROLE, "ADMIN")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldGetJoinResponsesForProject {

        @Test
        void shouldGetJoinResponsesForProject() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/join/responses", projectId)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.[0].id").value(1))
                    .andExpect(jsonPath("$.[0].respondedByUsername").value("frneek"))
                    .andExpect(jsonPath("$.[0].joinRequestId").value(1))
                    .andExpect(jsonPath("$.[0].status").value("ACCEPTED"))
                    .andExpect(jsonPath("$.[0].message").value("Hi Jarosław! Thank you for your interest in joining the Kanwise-Backend project. We are pleased to offer you a position on the team. Please let us know if you have any questions or if there is anything we can do to support you as you get started."))
                    .andExpect(jsonPath("$.[0].respondedAt").exists())
                    .andExpect(jsonPath("$.[0].links.[0].rel").value("responded-by"))
                    .andExpect(jsonPath("$.[0].links.[0].href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$.[0].links.[1].rel").value("join-request"))
                    .andExpect(jsonPath("$.[0].links.[1].href").value("http://localhost/join/request/1"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotGetJoinResponsesForProject {

        @Test
        void shouldNotGetJoinResponsesForProjectIfProjectDoesNotExist() throws Exception {
            // Given
            Long projectId = 999L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/join/responses", projectId)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetJoinResponsesForProjectWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/join/responses", projectId)
                            .header(USERNAME, "frneek")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetJoinResponsesForProjectWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/join/responses", projectId)
                            .header(ROLE, "ADMIN")
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }
}