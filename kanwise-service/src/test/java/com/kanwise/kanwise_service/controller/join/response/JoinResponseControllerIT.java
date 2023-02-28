package com.kanwise.kanwise_service.controller.join.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.kanwise_service.controller.DatabaseCleaner;
import com.kanwise.kanwise_service.model.join.response.command.CreateJoinResponseCommand;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class JoinResponseControllerIT {

    private final MockMvc mockMvc;
    private final DatabaseCleaner databaseCleaner;
    private final ObjectMapper objectMapper;

    @Autowired
    JoinResponseControllerIT(MockMvc mockMvc, DatabaseCleaner databaseCleaner, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.databaseCleaner = databaseCleaner;
        this.objectMapper = objectMapper;
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }

    @Nested
    class ShouldCreateJoinResponse {

        @Test
        void shouldCreateJoinResponseWithAcceptedStatus() throws Exception {
            // Given
            CreateJoinResponseCommand createJoinResponseCommand = CreateJoinResponseCommand.
                    builder()
                    .respondedByUsername("frneek")
                    .joinRequestId(3L)
                    .status("ACCEPTED")
                    .message("Welcome to Kanwise-DevOps!")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/response")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinResponseCommand)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.respondedByUsername").value(createJoinResponseCommand.respondedByUsername()))
                    .andExpect(jsonPath("$.joinRequestId").value(createJoinResponseCommand.joinRequestId()))
                    .andExpect(jsonPath("$.status").value(createJoinResponseCommand.status()))
                    .andExpect(jsonPath("$.message").value(createJoinResponseCommand.message()))
                    .andExpect(jsonPath("$._links.responded-by.href").value("http://localhost/member/" + createJoinResponseCommand.respondedByUsername()))
                    .andExpect(jsonPath("$._links.join-request.href").value("http://localhost/join/request/" + createJoinResponseCommand.joinRequestId()));
        }

        @Test
        void shouldCreateJoinResponseWithRejectedStatus() throws Exception {
            // Given
            CreateJoinResponseCommand createJoinResponseCommand = CreateJoinResponseCommand.
                    builder()
                    .respondedByUsername("frneek")
                    .joinRequestId(3L)
                    .status("REJECTED")
                    .message("We are sorry, but you are not qualified for Kanwise-DevOps!")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/response")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinResponseCommand)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.respondedByUsername").value(createJoinResponseCommand.respondedByUsername()))
                    .andExpect(jsonPath("$.joinRequestId").value(createJoinResponseCommand.joinRequestId()))
                    .andExpect(jsonPath("$.status").value(createJoinResponseCommand.status()))
                    .andExpect(jsonPath("$.message").value(createJoinResponseCommand.message()))
                    .andExpect(jsonPath("$._links.responded-by.href").value("http://localhost/member/" + createJoinResponseCommand.respondedByUsername()))
                    .andExpect(jsonPath("$._links.join-request.href").value("http://localhost/join/request/" + createJoinResponseCommand.joinRequestId()));
        }
    }

    @Nested
    class ShouldNotCreateJoinResponse {

        @Test
        void shouldNotCreateJoinResponseWithBlankRespondedByUsername() throws Exception {
            // Given
            CreateJoinResponseCommand createJoinResponseCommand = CreateJoinResponseCommand.
                    builder()
                    .respondedByUsername("")
                    .joinRequestId(3L)
                    .status("ACCEPTED")
                    .message("Welcome to Kanwise-DevOps!")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/response")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinResponseCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'respondedByUsername' && @.message == 'RESPONDED_BY_USERNAME_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateJoinResponseIfResponderDoesNotExist() throws Exception {
            // Given
            String nonExistingResponderUsername = "nonExistingResponderUsername";
            CreateJoinResponseCommand createJoinResponseCommand = CreateJoinResponseCommand.
                    builder()
                    .respondedByUsername(nonExistingResponderUsername)
                    .joinRequestId(3L)
                    .status("ACCEPTED")
                    .message("Welcome to Kanwise-DevOps!")
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", nonExistingResponderUsername)
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nonExistingResponderUsername)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"));
            // Then
            mockMvc.perform(post("/join/response")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinResponseCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"));
        }

        @Test
        void shouldNotCreateJoinResponseWithNullStatus() throws Exception {
            // Given
            CreateJoinResponseCommand createJoinResponseCommand = CreateJoinResponseCommand.
                    builder()
                    .respondedByUsername("frneek")
                    .joinRequestId(3L)
                    .status(null)
                    .message("Welcome to Kanwise-DevOps!")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/response")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinResponseCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'status' && @.message == 'STATUS_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateJoinResponseWithInvalidStatus() throws Exception {
            // Given
            CreateJoinResponseCommand createJoinResponseCommand = CreateJoinResponseCommand.
                    builder()
                    .respondedByUsername("frneek")
                    .joinRequestId(3L)
                    .status("INVALID_STATUS")
                    .message("Welcome to Kanwise-DevOps!")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/response")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinResponseCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'status' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.join.request.JoinRequestStatus')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateJoinResponseIfJoinRequestDoesNotExist() throws Exception {
            // Given
            Long nonExistingJoinRequestId = 999L;
            CreateJoinResponseCommand createJoinResponseCommand = CreateJoinResponseCommand.
                    builder()
                    .respondedByUsername("frneek")
                    .joinRequestId(nonExistingJoinRequestId)
                    .status("ACCEPTED")
                    .message("Welcome to Kanwise-DevOps!")
                    .build();
            // When
            mockMvc.perform(get("/join/request/{id}", nonExistingJoinRequestId)
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nonExistingJoinRequestId)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("JOIN_REQUEST_NOT_FOUND"));
            // Then
            mockMvc.perform(post("/join/response")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinResponseCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("JOIN_REQUEST_NOT_FOUND"));
        }

        @Test
        void shouldNotCreateJoinResponseIfJoinRequestIsNotPending() throws Exception {
            // Given
            Long nonPendingJoinRequestId = 1L;
            CreateJoinResponseCommand createJoinResponseCommand = CreateJoinResponseCommand.
                    builder()
                    .respondedByUsername("frneek")
                    .joinRequestId(nonPendingJoinRequestId)
                    .status("ACCEPTED")
                    .message("Welcome to Kanwise-DevOps!")
                    .build();
            // When
            mockMvc.perform(get("/join/request/{id}", nonPendingJoinRequestId)
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nonPendingJoinRequestId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(nonPendingJoinRequestId))
                    .andExpect(jsonPath("$.joinResponseId").exists());
            // Then
            mockMvc.perform(post("/join/response")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinResponseCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("JOIN_REQUEST_ALREADY_RESPONDED"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateJoinResponseIfJoinRequestWithBlankMessage() throws Exception {
            // Given
            CreateJoinResponseCommand createJoinResponseCommand = CreateJoinResponseCommand.
                    builder()
                    .respondedByUsername("frneek")
                    .joinRequestId(3L)
                    .status("ACCEPTED")
                    .message("")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/response")
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinResponseCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'message' && @.message == 'MESSAGE_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateJoinResponseWithoutUsernameHeader() throws Exception {
            // Given
            CreateJoinResponseCommand createJoinResponseCommand = CreateJoinResponseCommand.
                    builder()
                    .respondedByUsername("frneek")
                    .joinRequestId(3L)
                    .status("ACCEPTED")
                    .message("Welcome to Kanwise-DevOps!")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/response")
                            .header(ROLE, "USER")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinResponseCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateJoinResponseWithoutRoleHeader() throws Exception {
            // Given
            CreateJoinResponseCommand createJoinResponseCommand = CreateJoinResponseCommand.
                    builder()
                    .respondedByUsername("frneek")
                    .joinRequestId(3L)
                    .status("ACCEPTED")
                    .message("Welcome to Kanwise-DevOps!")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/join/response")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createJoinResponseCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldGetJoinResponse {

        @Test
        void shouldGetJoinResponse() throws Exception {
            // Given
            Long joinResponseId = 1L;
            // When
            // Then
            mockMvc.perform(get("/join/response/{id}", joinResponseId)
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(joinResponseId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(joinResponseId))
                    .andExpect(jsonPath("$.respondedByUsername").value("frneek"))
                    .andExpect(jsonPath("$.joinRequestId").value(1))
                    .andExpect(jsonPath("$.status").value("ACCEPTED"))
                    .andExpect(jsonPath("$.message").value("Hi Jarosław! Thank you for your interest in joining the Kanwise-Backend project. We are pleased to offer you a position on the team. Please let us know if you have any questions or if there is anything we can do to support you as you get started."))
                    .andExpect(jsonPath("$._links.responded-by.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.join-request.href").value("http://localhost/join/request/1"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotGetJoinResponse {

        @Test
        void shouldNotGetJoinResponseIfJoinResponseDoesNotExist() throws Exception {
            // Given
            Long nonExistingJoinResponseId = 100L;
            // When
            // Then
            mockMvc.perform(get("/join/response/{id}", nonExistingJoinResponseId)
                            .header(ROLE, "USER")
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nonExistingJoinResponseId)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("JOIN_RESPONSE_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetJoinResponseWithoutUsernameHeader() throws Exception {
            // Given
            Long joinResponseId = 1L;
            // When
            // Then
            mockMvc.perform(get("/join/response/{id}", joinResponseId)
                            .header(ROLE, "USER")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(joinResponseId)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotGetJoinResponseWithoutRoleHeader() throws Exception {
            // Given
            Long joinResponseId = 1L;
            // When
            // Then
            mockMvc.perform(get("/join/response/{id}", joinResponseId)
                            .header(USERNAME, "jaroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(joinResponseId)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }
}