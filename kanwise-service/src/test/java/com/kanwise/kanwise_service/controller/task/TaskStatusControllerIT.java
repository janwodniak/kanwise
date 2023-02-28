package com.kanwise.kanwise_service.controller.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.kanwise_service.controller.DatabaseCleaner;
import com.kanwise.kanwise_service.model.task_status.command.CreateTaskStatusCommand;
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
class TaskStatusControllerIT {

    private final MockMvc mockMvc;
    private final DatabaseCleaner databaseCleaner;
    private final ObjectMapper objectMapper;

    @Autowired
    TaskStatusControllerIT(MockMvc mockMvc, DatabaseCleaner databaseCleaner, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.databaseCleaner = databaseCleaner;
        this.objectMapper = objectMapper;
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }

    @Nested
    class ShouldCreateTaskStatus {

        @Test
        void shouldCreateTaskStatus() throws Exception {
            // Given
            CreateTaskStatusCommand createTaskStatusCommand = CreateTaskStatusCommand.builder()
                    .label("IN_PROGRESS")
                    .taskId(1L)
                    .setBy("frneek")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/status")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskStatusCommand)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(26))
                    .andExpect(jsonPath("$.taskId").value(createTaskStatusCommand.taskId()))
                    .andExpect(jsonPath("$.ongoing").value(true))
                    .andExpect(jsonPath("$.label").value(createTaskStatusCommand.label()))
                    .andExpect(jsonPath("$.setAt").isNotEmpty())
                    .andExpect(jsonPath("$.setTill").doesNotExist())
                    .andExpect(jsonPath("$.setBy").value(createTaskStatusCommand.setBy()))
                    .andExpect(jsonPath("$._links.task.href").value("http://localhost/task/1"))
                    .andExpect(jsonPath("$._links.setBy.href").value("http://localhost/member/frneek"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotCreateTaskStatus {

        @Test
        void shouldNotCreateTaskStatusIfTaskDoesNotExist() throws Exception {
            // Given
            Long taskId = 100L;
            CreateTaskStatusCommand createTaskStatusCommand = CreateTaskStatusCommand.builder()
                    .label("IN_PROGRESS")
                    .taskId(taskId)
                    .setBy("frneek")
                    .build();
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
            mockMvc.perform(post("/task/status")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskStatusCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("TASK_NOT_FOUND"))
                    .andDo(print());
        }


        @Test
        void shouldNotCreateTaskStatusWithNullLabel() throws Exception {
            // Given
            CreateTaskStatusCommand createTaskStatusCommand = CreateTaskStatusCommand.builder()
                    .label(null)
                    .taskId(1L)
                    .setBy("frneek")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/status")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskStatusCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'label' && @.message == 'LABEL_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskStatusWithInvalidLabelValue() throws Exception {
            // Given
            CreateTaskStatusCommand createTaskStatusCommand = CreateTaskStatusCommand.builder()
                    .label("INVALID")
                    .taskId(1L)
                    .setBy("frneek")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/status")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskStatusCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'label' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.task_status.TaskStatusLabel')]").exists())
                    .andDo(print());
        }


        @Test
        void shouldNotCreateTaskStatusIfSetByDoesNotExist() throws Exception {
            // Given
            CreateTaskStatusCommand createTaskStatusCommand = CreateTaskStatusCommand.builder()
                    .label("IN_PROGRESS")
                    .taskId(1L)
                    .setBy("nonExistingUsername")
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", createTaskStatusCommand.setBy())
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
            mockMvc.perform(post("/task/status")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskStatusCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskStatusWithoutRoleHeader() throws Exception {
            // Given
            CreateTaskStatusCommand createTaskStatusCommand = CreateTaskStatusCommand.builder()
                    .label("IN_PROGRESS")
                    .taskId(1L)
                    .setBy("frneek")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/status")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskStatusCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskStatusWithoutUsernameHeader() throws Exception {
            // Given
            CreateTaskStatusCommand createTaskStatusCommand = CreateTaskStatusCommand.builder()
                    .label("IN_PROGRESS")
                    .taskId(1L)
                    .setBy("frneek")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/status")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(createTaskStatusCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldFindTaskStatuses {

        @Test
        void shouldFindTaskStatuses() throws Exception {
            // Given
            Long taskId = 2L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}/statuses", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").exists())
                    .andExpect(jsonPath("$.content[0].id").value(2))
                    .andExpect(jsonPath("$.content[0].taskId").value(2))
                    .andExpect(jsonPath("$.content[0].ongoing").value(false))
                    .andExpect(jsonPath("$.content[0].label").value("TODO"))
                    .andExpect(jsonPath("$.content[0].setAt").exists())
                    .andExpect(jsonPath("$.content[0].setTill").exists())
                    .andExpect(jsonPath("$.content[0].setBy").value("frneek"))
                    .andExpect(jsonPath("$.content[0].links").exists())
                    .andExpect(jsonPath("$.content[0].links[0].rel").value("task"))
                    .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/task/2"))
                    .andExpect(jsonPath("$.content[0].links[1].rel").value("setBy"))
                    .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$.content[1].id").value(3))
                    .andExpect(jsonPath("$.content[1].taskId").value(2))
                    .andExpect(jsonPath("$.content[1].ongoing").value(true))
                    .andExpect(jsonPath("$.content[1].label").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.content[1].setAt").exists())
                    .andExpect(jsonPath("$.content[1].setBy").value("frneek"))
                    .andExpect(jsonPath("$.content[1].links").exists())
                    .andExpect(jsonPath("$.content[1].links[0].rel").value("task"))
                    .andExpect(jsonPath("$.content[1].links[0].href").value("http://localhost/task/2"))
                    .andExpect(jsonPath("$.content[1].links[1].rel").value("setBy"))
                    .andExpect(jsonPath("$.content[1].links[1].href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$.pageable").exists())
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotFindTaskStatuses {

        @Test
        void shouldNotFindTaskStatusesIfTaskDoesNotExist() throws Exception {
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
                    .andExpect(jsonPath("$.message").value("TASK_NOT_FOUND"));
            // Then
            mockMvc.perform(get("/task/{id}/statuses", taskId)
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
        void shouldNotFindTaskStatusesWithoutRoleHeader() throws Exception {
            // Given
            Long taskId = 2L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}/statuses", taskId)
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
        void shouldNotFindTaskStatusesWithoutUsernameHeader() throws Exception {
            // Given
            Long taskId = 2L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}/statuses", taskId)
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