package com.kanwise.kanwise_service.controller.join.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.kanwise_service.controller.DatabaseCleaner;
import com.kanwise.kanwise_service.model.join.request.command.CreateJoinRequestCommand;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class JoinRequestControllerIT {

    private final MockMvc mockMvc;
    private final DatabaseCleaner databaseCleaner;
    private final ObjectMapper objectMapper;

    @Autowired
    JoinRequestControllerIT(MockMvc mockMvc, DatabaseCleaner databaseCleaner, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.databaseCleaner = databaseCleaner;
        this.objectMapper = objectMapper;
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }

    @Nested
    class ShouldCreateJoinRequest {

        @Test
        void shouldCreateJoinRequest() throws Exception {
            // Given
            CreateJoinRequestCommand createJoinRequestCommand = CreateJoinRequestCommand.builder()
                    .projectId(3L)
                    .requestedByUsername("jaroslawPsikuta")
                    .message("I want to join this project!")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/request")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinRequestCommand)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.projectId").value(3L))
                    .andExpect(jsonPath("$.requestedByUsername").value("jaroslawPsikuta"))
                    .andExpect(jsonPath("$.message").value("I want to join this project!"))
                    .andExpect(jsonPath("$._links.requested-by.href").value("http://localhost/member/jaroslawPsikuta"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/3"));
        }
    }

    @Nested
    class ShouldNotCreateJoinRequest {

        @Test
        void shouldNotCreateJoinRequestWithNullProjectId() throws Exception {
            // Given
            CreateJoinRequestCommand createJoinRequestCommand = CreateJoinRequestCommand.builder()
                    .requestedByUsername("jaroslawPsikuta")
                    .message("I want to join this project!")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/request")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinRequestCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'projectId' && @.message == 'PROJECT_ID_NOT_NULL')]").exists());
        }

        @Test
        void shouldNotCreateJoinRequestIfProjectDoesNotExist() throws Exception {
            // Given
            long projectId = 999L;
            CreateJoinRequestCommand createJoinRequestCommand = CreateJoinRequestCommand.builder()
                    .projectId(projectId)
                    .requestedByUsername("jaroslawPsikuta")
                    .message("I want to join this project!")
                    .build();
            // When
            mockMvc.perform(get("/project/{projectId}", projectId)
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinRequestCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"));
            // Then
            mockMvc.perform(post("/join/request")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinRequestCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"));
        }

        @Test
        void shouldNotCreateJoinRequestWithBlankRequestedByUsername() throws Exception {
            // Given
            CreateJoinRequestCommand createJoinRequestCommand = CreateJoinRequestCommand.builder()
                    .projectId(3L)
                    .requestedByUsername(" ")
                    .message("I want to join this project!")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/request")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinRequestCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'requestedByUsername' && @.message == 'REQUESTED_BY_USERNAME_NOT_BLANK')]").exists());
        }

        @Test
        void shouldNotCreateJoinRequestIfRequesterDoesNotExist() throws Exception {
            // Given
            String nonExistingRequestedByUsername = "doesNotExist";
            CreateJoinRequestCommand createJoinRequestCommand = CreateJoinRequestCommand.builder()
                    .projectId(3L)
                    .requestedByUsername(nonExistingRequestedByUsername)
                    .message("I want to join this project!")
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", nonExistingRequestedByUsername)
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinRequestCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"));
            // Then
            mockMvc.perform(post("/join/request")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinRequestCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"));
        }

        @Test
        void shouldNotCreateJoinRequestWithBlankMessage() throws Exception {
            // Given
            CreateJoinRequestCommand createJoinRequestCommand = CreateJoinRequestCommand.builder()
                    .projectId(3L)
                    .requestedByUsername("jaroslawPsikuta")
                    .message(" ")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/request")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinRequestCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'message' && @.message == 'MESSAGE_NOT_BLANK')]").exists());
        }

        @Test
        void shouldNotCreateJoinRequestIfRequesterIsAlreadyAProjectMember() throws Exception {
            // Given
            CreateJoinRequestCommand createJoinRequestCommand = CreateJoinRequestCommand.builder()
                    .projectId(1L)
                    .requestedByUsername("jaroslawPsikuta")
                    .message("I want to join this project!")
                    .build();

            // When
            // Then
            mockMvc.perform(post("/join/request")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinRequestCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("MEMBER_WITH_USERNAME_jaroslawPsikuta_IS_ALREADY_ASSIGNED_TO_PROJECT_WITH_ID_1"));
        }

        @Test
        void shouldNotCreateJoinRequestWithoutRoleHeader() throws Exception {
            // Given
            CreateJoinRequestCommand createJoinRequestCommand = CreateJoinRequestCommand.builder()
                    .projectId(3L)
                    .requestedByUsername("jaroslawPsikuta")
                    .message("I want to join this project!")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/request")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinRequestCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"));
        }

        @Test
        void shouldNotCreateJoinRequestWithoutUsernameHeader() throws Exception {
            // Given
            CreateJoinRequestCommand createJoinRequestCommand = CreateJoinRequestCommand.builder()
                    .projectId(3L)
                    .requestedByUsername("jaroslawPsikuta")
                    .message("I want to join this project!")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/request")
                            .header(ROLE, "USER")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinRequestCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"));
        }
    }

    @Nested
    class ShouldGetJoinRequest {

        @Test
        void shouldGetJoinRequest() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/join/request/1")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.projectId").value(1))
                    .andExpect(jsonPath("$.requestedByUsername").value("jaroslawPsikuta"))
                    .andExpect(jsonPath("$.joinResponseId").value(1))
                    .andExpect(jsonPath("$.requestedAt").value("2022-12-16T10:07:42.821125"))
                    .andExpect(jsonPath("$.message").value("Hi team, I am a software developer interested in joining the Kanwise-Backend project. I have experience with Java, Spring Boot, and RESTful APIs. Please let me know if you have any openings."))
                    .andExpect(jsonPath("$._links.requested-by.href").value("http://localhost/member/jaroslawPsikuta"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.responded-by.href").value("http://localhost/member/frneek"));
        }

        @Nested
        class ShouldNotGetJoinRequest {

            @Test
            void shouldNotGetJoinRequestIfJoinRequestDoesNotExist() throws Exception {
                // Given
                // When
                // Then
                mockMvc.perform(get("/join/request/100")
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN")
                                .header(USERNAME, "admin"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                        .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                        .andExpect(jsonPath("$.message").value("JOIN_REQUEST_NOT_FOUND"));
            }

            @Test
            void shouldNotGetJoinRequestIfJoinRequestWithoutRoleHeader() throws Exception {
                // Given
                // When
                // Then
                mockMvc.perform(get("/join/request/1")
                                .contentType(APPLICATION_JSON)
                                .header(USERNAME, "admin"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                        .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"));
            }

            @Test
            void shouldNotGetJoinRequestIfJoinRequestWithoutUsernameHeader() throws Exception {
                // Given
                // When
                // Then
                mockMvc.perform(get("/join/request/1")
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                        .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"));
            }
        }
    }
}