package com.kanwise.report_service.controller.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.report_service.controller.DatabaseCleaner;
import com.kanwise.report_service.model.subscriber.command.CreateSubscriberCommand;
import com.kanwise.report_service.model.subscriber.command.EditSubscriberCommand;
import com.kanwise.report_service.model.subscriber.command.EditSubscriberPartiallyCommand;
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

import static com.kanwise.report_service.model.http.HttpHeader.ROLE;
import static com.kanwise.report_service.model.http.HttpHeader.USERNAME;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class SubscriberControllerIT {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DatabaseCleaner databaseCleaner;

    @Autowired
    SubscriberControllerIT(MockMvc mockMvc, ObjectMapper objectMapper, DatabaseCleaner databaseCleaner) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.databaseCleaner = databaseCleaner;
    }

    @BeforeEach
    void setUp() throws LiquibaseException {
        databaseCleaner.setUp();
    }

    @Nested
    class ShouldCreateSubscriber {

        @Test
        void shouldCreateSubscriber() throws Exception {
            // Given
            CreateSubscriberCommand createSubscriberCommand = CreateSubscriberCommand.builder()
                    .username("janroslawPsikuta")
                    .email("jaroslawPsikuta.kanwise@gmail.com")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/subscriber")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createSubscriberCommand)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("janroslawPsikuta"))
                    .andExpect(jsonPath("$.email").value("jaroslawPsikuta.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(0))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/janroslawPsikuta/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/janroslawPsikuta/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());

            mockMvc.perform(get("/subscriber/janroslawPsikuta")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("janroslawPsikuta"))
                    .andExpect(jsonPath("$.email").value("jaroslawPsikuta.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(0))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/janroslawPsikuta/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/janroslawPsikuta/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotCreateSubscriber {

        @Test
        void shouldNotCreateSubscriberWithNullUsername() throws Exception {
            // Given
            CreateSubscriberCommand createSubscriberCommand = CreateSubscriberCommand.builder()
                    .username(null)
                    .email("jaroslawPsikuta.kanwise@gmail.com")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/subscriber")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createSubscriberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateSubscriberWithBlankUsername() throws Exception {
            // Given
            CreateSubscriberCommand createSubscriberCommand = CreateSubscriberCommand.builder()
                    .username(" ")
                    .email("jaroslawPsikuta.kanwise@gmail.com")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/subscriber")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createSubscriberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateSubscriberWithNullEmail() throws Exception {
            // Given
            CreateSubscriberCommand createSubscriberCommand = CreateSubscriberCommand.builder()
                    .username("janroslawPsikuta")
                    .email(null)
                    .build();
            // When
            // Then
            mockMvc.perform(post("/subscriber")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createSubscriberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'EMAIL_NOT_BLANK')]").exists())
                    .andDo(print());
        }


        @Test
        void shouldNotCreateSubscriberWithInvalidEmailPattern() throws Exception {
            // Given
            CreateSubscriberCommand createSubscriberCommand = CreateSubscriberCommand.builder()
                    .username("janroslawPsikuta")
                    .email("jaroslawPsikuta.kanwise")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/subscriber")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createSubscriberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'INVALID_EMAIL_PATTERN')]").exists())
                    .andDo(print());
        }
    }

    @Nested
    class ShouldGetSubscriber {

        @Test
        void shouldGetSubscriber() throws Exception {
            // Given
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotGetSubscriber {

        @Test
        void shouldNotGetSubscriberWithIfSubscriberDoesNotExist() throws Exception {
            // Given
            String nonExistingUsername = "nonExistingUsername";
            // When
            // Then
            mockMvc.perform(get("/subscriber/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(nonExistingUsername)))
                    .andDo(print());
        }

        @Test
        void shouldNotGetSubscriberWithoutRoleHeader() throws Exception {
            // Given
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(get("/subscriber/{username}", username)
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
        void shouldNotGetSubscriberWithoutUsernameHeader() throws Exception {
            // Given
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(get("/subscriber/{username}", username)
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
    class ShouldGetAllSubscribers {

        @Test
        void shouldGetAllSubscribers() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/subscriber")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.[*].username").value(hasItems("marcin123", "zbyszek", "frneek")))
                    .andExpect(jsonPath("$.[*].email").value(hasItems("frneek.kanwise@gmail.com", "marcin123.kanwise@gmail.com", "zbigniew.kanwise@gmail.com")))
                    .andExpect(jsonPath("$.[*].personalReportsCount").value(hasItems(0, 0, 2)))
                    .andExpect(jsonPath("$.[*].projectReportsCount").value(hasItems(0, 0, 0)))
                    .andExpect(jsonPath("$.[*].links[*].rel").value(hasItems("personal-reports", "project-reports")))
                    .andExpect(jsonPath("$.[*].links[*].href").value(hasItems("http://localhost/subscriber/marcin123/reports/personal{?status}", "http://localhost/subscriber/marcin123/reports/project{?status}")))
                    .andExpect(jsonPath("$.[*].links[*].href").value(hasItems("http://localhost/subscriber/zbyszek/reports/personal{?status}", "http://localhost/subscriber/zbyszek/reports/project{?status}")))
                    .andExpect(jsonPath("$.[*].links[*].href").value(hasItems("http://localhost/subscriber/frneek/reports/personal{?status}", "http://localhost/subscriber/frneek/reports/project{?status}")))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotGetAllSubscribers {

        @Test
        void shouldNotGetAllSubscribersWithoutRoleHeader() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/subscriber")
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
        void shouldNotGetAllSubscribersWithoutUsernameHeader() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/subscriber")
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
    class ShouldDeleteSubscriber {

        @Test
        void shouldDeleteSubscriber() throws Exception {
            // Given
            String usernameToDelete = "frneek";
            // When
            mockMvc.perform(get("/subscriber/{username}", usernameToDelete)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
            // Then
            mockMvc.perform(delete("/subscriber/{username}", usernameToDelete)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            mockMvc.perform(get("/subscriber/{username}", usernameToDelete)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(usernameToDelete)))
                    .andDo(print());

        }
    }

    @Nested
    class ShouldNotDeleteSubscriber {

        @Test
        void shouldNotDeleteSubscriberWithIfSubscriberDoesNotExist() throws Exception {
            // Given
            String nonExistingUsername = "nonExistingUsername";
            // When
            mockMvc.perform(delete("/subscriber/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(nonExistingUsername)))
                    .andDo(print());
            // Then
            mockMvc.perform(delete("/subscriber/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(nonExistingUsername)))
                    .andDo(print());
        }

        @Test
        void shouldNotDeleteSubscriberWithoutRoleHeader() throws Exception {
            // Given
            String usernameToDelete = "frneek";
            // When
            mockMvc.perform(get("/subscriber/{username}", usernameToDelete)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
            // Then
            mockMvc.perform(delete("/subscriber/{username}", usernameToDelete)
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
        void shouldNotDeleteSubscriberWithoutUsernameHeader() throws Exception {
            // Given
            String usernameToDelete = "frneek";
            // When
            mockMvc.perform(get("/subscriber/{username}", usernameToDelete)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
            // Then
            mockMvc.perform(delete("/subscriber/{username}", usernameToDelete)
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
    class ShouldEditSubscriber {

        @Test
        void shouldEditSubscriber() throws Exception {
            // Given
            String username = "frneek";
            String editedEmail = "edited.kanwise@gmail.com";
            EditSubscriberCommand editSubscriberCommand = EditSubscriberCommand.builder()
                    .username(username)
                    .email(editedEmail)
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
            // Then
            mockMvc.perform(put("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value(editedEmail))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());

            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value(editedEmail))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotEditSubscriber {

        @Test
        void shouldNotEditSubscriberWithNullUsername() throws Exception {
            // Given
            String username = "frneek";
            EditSubscriberCommand editSubscriberCommand = EditSubscriberCommand.builder()
                    .username(username)
                    .email(null)
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
            // Then
            mockMvc.perform(put("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'EMAIL_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditSubscriberWithBlankUsername() throws Exception {
            // Given
            String username = "frneek";
            EditSubscriberCommand editSubscriberCommand = EditSubscriberCommand.builder()
                    .username(" ")
                    .email("frneek.kanwise@gmail.com")
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
            // Then
            mockMvc.perform(put("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditSubscriberWithNullEmail() throws Exception {
            // Given
            String username = "frneek";
            EditSubscriberCommand editSubscriberCommand = EditSubscriberCommand.builder()
                    .username(username)
                    .email(null)
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
            // Then
            mockMvc.perform(put("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'EMAIL_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditSSubscriberWithInvalidEmailPattern() throws Exception {
            // Given
            String username = "frneek";
            EditSubscriberCommand editSubscriberCommand = EditSubscriberCommand.builder()
                    .username(username)
                    .email("frneek.kanwise")
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
            // Then
            mockMvc.perform(put("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'INVALID_EMAIL_PATTERN')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditSubscriberWithIfSubscriberDoesNotExist() throws Exception {
            // Given
            String nonExistingUsername = "nonExistingUsername";
            EditSubscriberCommand editSubscriberCommand = EditSubscriberCommand.builder()
                    .username(nonExistingUsername)
                    .email("nonExisting.kanwise@gmail.com")
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(nonExistingUsername)))
                    .andDo(print());
            // Then
            mockMvc.perform(put("/subscriber/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(nonExistingUsername)))
                    .andDo(print());
        }

        @Test
        void shouldNotEditSubscriberWithoutRoleHeader() throws Exception {
            // Given
            String username = "frneek";
            String editedEmail = "edited.kanwise@gmail.com";
            EditSubscriberCommand editSubscriberCommand = EditSubscriberCommand.builder()
                    .username(username)
                    .email(editedEmail)
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
            // Then
            mockMvc.perform(put("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotEditSubscriberWithoutUsernameHeader() throws Exception {
            // Given
            String username = "frneek";
            String editedEmail = "edited.kanwise@gmail.com";
            EditSubscriberCommand editSubscriberCommand = EditSubscriberCommand.builder()
                    .username(username)
                    .email(editedEmail)
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
            // Then
            mockMvc.perform(put("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(editSubscriberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldEditSubscriberPartially {

        @Test
        void shouldEditSubscriberPartiallyWithUsernameOnly() throws Exception {
            // Given
            String username = "frneek";
            String editedUsername = "editedfrneek";
            EditSubscriberPartiallyCommand editSubscriberPartiallyCommand = EditSubscriberPartiallyCommand.builder()
                    .username(editedUsername)
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberPartiallyCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(editedUsername))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/%s/reports/personal{?status}".formatted(editedUsername)))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/%s/reports/project{?status}".formatted(editedUsername)))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true));

            mockMvc.perform(get("/subscriber/{username}", editedUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(editedUsername))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/%s/reports/personal{?status}".formatted(editedUsername)))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/%s/reports/project{?status}".formatted(editedUsername)))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
        }

        @Test
        void shouldEditSubscriberPartiallyWithEmailOnly() throws Exception {
            // Given
            String username = "frneek";
            String editedEmail = "edited.kanwise@gmail.com";
            EditSubscriberPartiallyCommand editSubscriberPartiallyCommand = EditSubscriberPartiallyCommand.builder()
                    .email(editedEmail)
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberPartiallyCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value(editedEmail))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/%s/reports/personal{?status}".formatted(username)))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/%s/reports/project{?status}".formatted(username)))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true));

            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value(editedEmail))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/%s/reports/personal{?status}".formatted(username)))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/%s/reports/project{?status}".formatted(username)))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true));
        }
    }

    @Nested
    class ShouldNotEditSubscriberPartially {

        @Test
        void shouldNotEditSubscriberPartiallyWithBlankUsername() throws Exception {
            // Given
            String username = "frneek";
            EditSubscriberPartiallyCommand editSubscriberPartiallyCommand = EditSubscriberPartiallyCommand.builder()
                    .username("   ")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberPartiallyCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NULL_OR_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditSubscriberPartiallyWithInvalidEmailPattern() throws Exception {
            // Given
            String username = "frneek";
            EditSubscriberPartiallyCommand editSubscriberPartiallyCommand = EditSubscriberPartiallyCommand.builder()
                    .email("invalid-email")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberPartiallyCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'email' && @.message == 'INVALID_EMAIL_PATTERN')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditSubscriberPartiallyWithIfSubscriberDoesNotExist() throws Exception {
            // Given
            String nonExistingUsername = "nonExistingUsername";
            EditSubscriberPartiallyCommand editSubscriberPartiallyCommand = EditSubscriberPartiallyCommand.builder()
                    .email("edited.kanwise@gmail.com")
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(nonExistingUsername)))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/subscriber/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberPartiallyCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(nonExistingUsername)))
                    .andDo(print());
        }

        @Test
        void shouldNotEditSubscriberPartiallyWithoutRoleHeader() throws Exception {
            // Given
            String username = "frneek";
            String editedEmail = "edited.kanwise@gmail.com";
            EditSubscriberPartiallyCommand editSubscriberCommand = EditSubscriberPartiallyCommand.builder()
                    .email(editedEmail)
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editSubscriberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotEditSubscriberPartiallyWithoutUsernameHeader() throws Exception {
            // Given
            String username = "frneek";
            String editedEmail = "edited.kanwise@gmail.com";
            EditSubscriberPartiallyCommand editSubscriberCommand = EditSubscriberPartiallyCommand.builder()
                    .email(editedEmail)
                    .build();
            // When
            mockMvc.perform(get("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("frneek.kanwise@gmail.com"))
                    .andExpect(jsonPath("$.personalReportsCount").value(2))
                    .andExpect(jsonPath("$.projectReportsCount").value(0))
                    .andExpect(jsonPath("$._links.personal-reports.href").value("http://localhost/subscriber/frneek/reports/personal{?status}"))
                    .andExpect(jsonPath("$._links.personal-reports.templated").value(true))
                    .andExpect(jsonPath("$._links.project-reports.href").value("http://localhost/subscriber/frneek/reports/project{?status}"))
                    .andExpect(jsonPath("$._links.project-reports.templated").value(true))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/subscriber/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(editSubscriberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }


    @Nested
    class ShouldGetSubscriberPersonalReportsJobs {

        @Test
        void shouldGetSubscriberPersonalReportsJobs() throws Exception {
            // Given
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(get("/subscriber/{username}/reports/personal", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.[*].cron").value(hasItems("0 0/1 * * * ?", "0 0/1 * * * ?")))
                    .andExpect(jsonPath("$.[*].description").value(hasItems("every minute", "every minute")))
                    .andExpect(jsonPath("$.[*].id").value(hasItems("8d5d705e-6270-481b-b7bd-457fb3c49164", "ecae2660-94ef-4636-8c7b-f1cab90f29d8")))
                    .andExpect(jsonPath("$.[*].subscriberUsername").value(hasItems("frneek", "frneek")))
                    .andExpect(jsonPath("$.[*].startDate").value(hasItems("2022-12-11T23:00:00", "2022-12-04T23:00:00")))
                    .andExpect(jsonPath("$.[*].endDate").value(hasItems("2022-12-30T23:00:00", "2023-04-28T22:00:00")))
                    .andExpect(jsonPath("$.[*].status").value(hasItems("STOPPED", "STOPPED")))
                    .andExpect(jsonPath("$.[*].links[*].rel").value(hasItems("subscriber", "personal-reports", "project-reports")))
                    .andExpect(jsonPath("$.[*].links[*].href").value(hasItems("http://localhost/subscriber/frneek", "http://localhost/subscriber/frneek/reports/personal{?status}", "http://localhost/subscriber/frneek/reports/project{?status}")));
        }
    }


    @Nested
    class ShouldNotGetSubscriberPersonalReportsJobs {

        @Test
        void shouldNotGetSubscriberPersonalReportsJobsWithIfSubscriberDoesNotExist() throws Exception {
            // Given
            String nonExistingUsername = "nonExistingUsername";
            // When
            mockMvc.perform(get("/subscriber/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(nonExistingUsername)))
                    .andDo(print());
            // Then
            mockMvc.perform(get("/subscriber/{username}/reports/personal", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(nonExistingUsername)))
                    .andDo(print());
        }

        @Test
        void shouldNotGetSubscriberPersonalReportsJobsWithoutRoleHeader() throws Exception {
            // Given
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(get("/subscriber/{username}/reports/personal", username)
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
        void shouldNotGetSubscriberPersonalReportsJobsWithoutUsernameHeader() throws Exception {
            // Given
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(get("/subscriber/{username}/reports/personal", username)
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
    class ShouldGetSubscriberProjectReportsJobs {

        @Test
        void shouldGetSubscriberProjectReportsJobs() throws Exception {
            // TODO: 2021-09-29
        }
    }

    @Nested
    class ShouldNotGetSubscriberProjectReportsJobs {

        @Test
        void shouldNotGetSubscriberProjectReportsJobsWithIfSubscriberDoesNotExist() throws Exception {
            // Given
            String nonExistingUsername = "nonExistingUsername";
            // When
            mockMvc.perform(get("/subscriber/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(nonExistingUsername)))
                    .andDo(print());
            // Then
            mockMvc.perform(get("/subscriber/{username}/reports/project", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("SUBSCRIBER_WITH_USERNAME_%s_NOT_FOUND".formatted(nonExistingUsername)))
                    .andDo(print());
        }

        @Test
        void shouldNotGetSubscriberProjectReportsJobsWithoutRoleHeader() throws Exception {
            // Given
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(get("/subscriber/{username}/reports/project", username)
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
        void shouldNotGetSubscriberProjectReportsJobsWithoutUsernameHeader() throws Exception {
            // Given
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(get("/subscriber/{username}/reports/project", username)
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