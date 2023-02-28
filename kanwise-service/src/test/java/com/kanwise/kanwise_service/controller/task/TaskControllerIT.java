package com.kanwise.kanwise_service.controller.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.kanwise_service.controller.DatabaseCleaner;
import com.kanwise.kanwise_service.model.task.command.CreateTaskCommand;
import com.kanwise.kanwise_service.model.task.command.EditTaskCommand;
import com.kanwise.kanwise_service.model.task.command.EditTaskPartiallyCommand;
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

import java.time.Duration;
import java.util.Set;

import static com.kanwise.kanwise_service.model.http.HttpHeader.ROLE;
import static com.kanwise.kanwise_service.model.http.HttpHeader.USERNAME;
import static com.kanwise.kanwise_service.model.task.command.CreateTaskCommand.builder;
import static java.time.Duration.of;
import static java.time.Duration.ofHours;
import static java.time.temporal.ChronoUnit.HOURS;
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
class TaskControllerIT {

    private final MockMvc mockMvc;
    private final DatabaseCleaner databaseCleaner;
    private final ObjectMapper objectMapper;


    @Autowired
    TaskControllerIT(MockMvc mockMvc, DatabaseCleaner databaseCleaner, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.databaseCleaner = databaseCleaner;
        this.objectMapper = objectMapper;
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }

    @Nested
    class ShouldCreateTask {

        @Test
        void shouldCreateTask() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.taskId").value(16))
                    .andExpect(jsonPath("$.projectId").value(createTaskCommand.projectId()))
                    .andExpect(jsonPath("$.authorUsername").value(createTaskCommand.authorUsername()))
                    .andExpect(jsonPath("$.assignedMembersCount").value(1))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value(createTaskCommand.title()))
                    .andExpect(jsonPath("$.description").value(createTaskCommand.description()))
                    .andExpect(jsonPath("$.priority").value(createTaskCommand.priority()))
                    .andExpect(jsonPath("$.type").value(createTaskCommand.type()))
                    .andExpect(jsonPath("$.estimatedTime").value(createTaskCommand.estimatedTime().toString()))
                    .andExpect(jsonPath("$.currentStatus").value(createTaskCommand.currentStatus()))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/16/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/16/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/16/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/16/statuses"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotCreateTask {

        @Test
        void shouldNotCreateTaskWithNullTitle() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title(null)
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'title' && @.message == 'TITLE_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithBlankTitle() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title(" ")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'title' && @.message == 'TITLE_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithNullDescription() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description(null)
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'description' && @.message == 'DESCRIPTION_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithBlankDescription() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description(" ")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'description' && @.message == 'DESCRIPTION_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithNullEstimatedTime() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(null)
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'estimatedTime' && @.message == 'ESTIMATED_TIME_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithNullAuthorUsername() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername(null)
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'authorUsername' && @.message == 'AUTHOR_USERNAME_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithBlankAuthorUsername() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername(" ")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'authorUsername' && @.message == 'AUTHOR_USERNAME_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskIfAuthorDoesNotExist() throws Exception {
            // Given
            String nonExistingUsername = "nonExistingUsername";
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername(nonExistingUsername)
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"));
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithNullPriority() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority(null)
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'priority' && @.message == 'PRIORITY_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithInvalidPriorityValue() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("INVALID")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'priority' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.task.TaskPriority')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithNullType() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type(null)
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'type' && @.message == 'TYPE_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithInvalidTypeValue() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("INVALID")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'type' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.task.TaskType')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithNullProjectId() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(null)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'projectId' && @.message == 'PROJECT_ID_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskIfProjectDoesNotExist() throws Exception {
            // Given
            Long nonExistingProjectId = 999L;
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(nonExistingProjectId)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
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
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithNullMembersUsernames() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(null)
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'membersUsernames' && @.message == 'MEMBERS_USERNAMES_NOT_NULL')]").exists())
                    .andDo(print());
        }


        @Test
        void shouldNotCreateTaskIfMemberDoesNotExist() throws Exception {
            // Given
            String nonExistingMemberUsername = "nonExistingMemberUsername";
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of(nonExistingMemberUsername))
                    .currentStatus("TODO")
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", nonExistingMemberUsername)
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
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithNullCurrentStatus() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus(null)
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'currentStatus' && @.message == 'CURRENT_STATUS_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskWithInvalidCurrentStatusValue() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("INVALID")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'currentStatus' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.task_status.TaskStatusLabel')]").exists())
                    .andDo(print());
        }


        @Test
        void shouldNotCreateTaskWithoutRoleHeader() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());

        }

        @Test
        void shouldNotCreateTaskWithoutUsernameHeader() throws Exception {
            // Given
            CreateTaskCommand createTaskCommand = builder()
                    .title("title")
                    .description("description")
                    .estimatedTime(of(1, HOURS))
                    .authorUsername("frneek")
                    .priority("LOW")
                    .type("BUG")
                    .projectId(1L)
                    .membersUsernames(Set.of("frneek"))
                    .currentStatus("TODO")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(createTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldFindTask {

        @Test
        void shouldFindTask() throws Exception {
            // Given
            Long taskId = 1L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotFindTask {

        @Test
        void shouldNotFindTaskIfTaskDoesNotExist() throws Exception {
            // Given
            Long taskId = 100L;
            // When
            // Then
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
        }

        @Test
        void shouldNotFindTaskWithoutRoleHeader() throws Exception {
            // Given
            Long taskId = 1L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}", taskId)
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
        void shouldNotFindTaskWithoutUsernameHeader() throws Exception {
            // Given
            Long taskId = 1L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}", taskId)
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
    class ShouldDeleteTask {

        @Test
        void shouldDeleteTask() throws Exception {
            // Given
            Long taskId = 1L;
            // When
            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
            // Then
            mockMvc.perform(delete("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNoContent())
                    .andDo(print());


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
        }
    }

    @Nested
    class ShouldNotDeleteTask {

        @Test
        void shouldNotDeleteTaskIfTaskDoesNotExist() throws Exception {
            // Given
            Long taskId = 100L;
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
            mockMvc.perform(delete("/task/{id}", taskId)
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
        void shouldNotDeleteTaskWithoutRoleHeader() throws Exception {
            // Given
            Long taskId = 1L;
            // When
            // Then
            mockMvc.perform(delete("/task/{id}", taskId)
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
        void shouldNotDeleteTaskWithoutUsernameHeader() throws Exception {
            // Given
            Long taskId = 1L;
            // When
            // Then
            mockMvc.perform(delete("/task/{id}", taskId)
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
    class ShouldEditTask {

        @Test
        void shouldEditTask() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description("Edited description")
                    .estimatedTime(ofHours(2))
                    .currentStatus("IN_PROGRESS")
                    .priority("HIGH")
                    .type("BUG")
                    .build();
            // When
            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
            // Then
            mockMvc.perform(put("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Edited title"))
                    .andExpect(jsonPath("$.description").value("Edited description"))
                    .andExpect(jsonPath("$.priority").value("HIGH"))
                    .andExpect(jsonPath("$.type").value("BUG"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT2H"))
                    .andExpect(jsonPath("$.currentStatus").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());

            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Edited title"))
                    .andExpect(jsonPath("$.description").value("Edited description"))
                    .andExpect(jsonPath("$.priority").value("HIGH"))
                    .andExpect(jsonPath("$.type").value("BUG"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT2H"))
                    .andExpect(jsonPath("$.currentStatus").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotEditTask {

        @Test
        void shouldNotEditTaskIfTaskDoesNotExist() throws Exception {
            // Given
            Long nonExistingTaskId = 999L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description("Edited description")
                    .estimatedTime(ofHours(2))
                    .currentStatus("IN_PROGRESS")
                    .priority("HIGH")
                    .type("BUG")
                    .build();
            // When
            mockMvc.perform(get("/task/{id}", nonExistingTaskId)
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
            mockMvc.perform(put("/task/{id}", nonExistingTaskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("TASK_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWitNullTitle() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title(null)
                    .description("Edited description")
                    .estimatedTime(ofHours(2))
                    .currentStatus("IN_PROGRESS")
                    .priority("HIGH")
                    .type("BUG")
                    .build();
            // When
            mockMvc.perform(put("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'title' && @.message == 'TITLE_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWithBlankTitle() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title(" ")
                    .description("Edited description")
                    .estimatedTime(ofHours(2))
                    .currentStatus("IN_PROGRESS")
                    .priority("HIGH")
                    .type("BUG")
                    .build();
            // When
            mockMvc.perform(put("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'title' && @.message == 'TITLE_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWithNullDescription() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description(null)
                    .estimatedTime(ofHours(2))
                    .currentStatus("IN_PROGRESS")
                    .priority("HIGH")
                    .type("BUG")
                    .build();
            // When
            mockMvc.perform(put("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'description' && @.message == 'DESCRIPTION_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWithBlankDescription() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description(" ")
                    .estimatedTime(ofHours(2))
                    .currentStatus("IN_PROGRESS")
                    .priority("HIGH")
                    .type("BUG")
                    .build();
            // When
            mockMvc.perform(put("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'description' && @.message == 'DESCRIPTION_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWithNullEstimatedTime() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description("Edited description")
                    .estimatedTime(null)
                    .currentStatus("IN_PROGRESS")
                    .priority("HIGH")
                    .type("BUG")
                    .build();
            // When
            mockMvc.perform(put("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'estimatedTime' && @.message == 'ESTIMATED_TIME_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWithNullCurrentStatus() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description("Edited description")
                    .estimatedTime(ofHours(2))
                    .currentStatus(null)
                    .priority("HIGH")
                    .type("BUG")
                    .build();
            // When
            mockMvc.perform(put("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'currentStatus' && @.message == 'CURRENT_STATUS_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWithInvalidCurrentStatusValue() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description("Edited description")
                    .estimatedTime(ofHours(2))
                    .currentStatus("INVALID")
                    .priority("HIGH")
                    .type("BUG")
                    .build();
            // When
            mockMvc.perform(put("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'currentStatus' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.task_status.TaskStatusLabel')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWithNullPriority() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description("Edited description")
                    .estimatedTime(ofHours(2))
                    .currentStatus("IN_PROGRESS")
                    .priority(null)
                    .type("BUG")
                    .build();
            // When
            mockMvc.perform(put("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'priority' && @.message == 'PRIORITY_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWithInvalidPriorityValue() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description("Edited description")
                    .estimatedTime(ofHours(2))
                    .currentStatus("IN_PROGRESS")
                    .priority("INVALID")
                    .type("BUG")
                    .build();
            // When
            mockMvc.perform(put("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'priority' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.task.TaskPriority')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWithNullType() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description("Edited description")
                    .estimatedTime(ofHours(2))
                    .currentStatus("IN_PROGRESS")
                    .priority("HIGH")
                    .type(null)
                    .build();
            // When
            mockMvc.perform(put("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'type' && @.message == 'TYPE_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWithInvalidTypeValue() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description("Edited description")
                    .estimatedTime(ofHours(2))
                    .currentStatus("IN_PROGRESS")
                    .priority("HIGH")
                    .type("INVALID")
                    .build();
            // When
            mockMvc.perform(put("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'type' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.task.TaskType')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWithoutRoleHeader() throws Exception {
            // Given
            Long nonExistingTaskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description("Edited description")
                    .estimatedTime(ofHours(2))
                    .currentStatus("IN_PROGRESS")
                    .priority("HIGH")
                    .type("BUG")
                    .build();
            // When
            // Then
            mockMvc.perform(put("/task/{id}", nonExistingTaskId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskWithoutUsernameHeader() throws Exception {
            // Given
            Long nonExistingTaskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title("Edited title")
                    .description("Edited description")
                    .estimatedTime(ofHours(2))
                    .currentStatus("IN_PROGRESS")
                    .priority("HIGH")
                    .type("BUG")
                    .build();
            // When
            // Then
            mockMvc.perform(put("/task/{id}", nonExistingTaskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldEditTaskPartially {

        @Test
        void shouldEditTaskPartiallyWithTitleOnly() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskPartiallyCommand editTaskCommand = EditTaskPartiallyCommand.builder()
                    .title("Edited title")
                    .build();
            // When
            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value(editTaskCommand.title()))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());

            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value(editTaskCommand.title()))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
        }

        @Test
        void shouldEditTaskPartiallyWithDescriptionOnly() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .description("Edited description")
                    .build();
            // When
            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value(editTaskCommand.description()))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());

            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value(editTaskCommand.description()))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
        }

        @Test
        void shouldEditTaskPartiallyWithEstimatedTimeOnly() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .estimatedTime(Duration.ofHours(20))
                    .build();
            // When
            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value(editTaskCommand.estimatedTime().toString()))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());

            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value(editTaskCommand.estimatedTime().toString()))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
        }

        @Test
        void shouldEditTaskPartiallyWithCurrentStatusOnly() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .currentStatus("IN_PROGRESS")
                    .build();
            // When
            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value(editTaskCommand.currentStatus()))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());

            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value(editTaskCommand.currentStatus()))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
        }

        @Test
        void shouldEditTaskPartiallyWithPriorityOnly() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .priority("HIGH")
                    .build();
            // When
            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value(editTaskCommand.priority()))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());

            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value(editTaskCommand.priority()))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
        }

        @Test
        void shouldEditTaskPartiallyWithTypeOnly() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .type("BUG")
                    .build();
            // When
            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value("NEW_FEATURE"))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value(editTaskCommand.type()))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());

            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.projectId").value(1L))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.assignedMembersCount").value(2))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.statusesCount").value(1))
                    .andExpect(jsonPath("$.title").value("Implement APIs for creating and updating kanban boards"))
                    .andExpect(jsonPath("$.description").value("This task involves developing the APIs that allow users to create and update kanban boards in the application."))
                    .andExpect(jsonPath("$.priority").value("NORMAL"))
                    .andExpect(jsonPath("$.type").value(editTaskCommand.type()))
                    .andExpect(jsonPath("$.estimatedTime").value("PT24H"))
                    .andExpect(jsonPath("$.currentStatus").value("TODO"))
                    .andExpect(jsonPath("$._links.project.href").value("http://localhost/project/1"))
                    .andExpect(jsonPath("$._links.author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotEditTaskPartially {

        @Test
        void shouldNotEditTaskPartiallyWithBlankTitle() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .title(" ")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'title' && @.message == 'TITLE_NULL_OR_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskPartiallyWithBlankDescription() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .description(" ")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'description' && @.message == 'DESCRIPTION_NULL_OR_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskPartiallyWithInvalidCurrentStatusValue() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .currentStatus("INVALID")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'currentStatus' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.task_status.TaskStatusLabel')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskPartiallyWithInvalidPriorityValue() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .priority("INVALID")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'priority' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.task.TaskPriority')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskPartiallyWithInvalidTypeValue() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .type("INVALID")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'type' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.task.TaskType')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditTaskPartiallyWithoutRoleHeader() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .estimatedTime(Duration.ofHours(33))
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());

        }

        @Test
        void shouldNotEditTaskPartiallyWithoutUsernameHeader() throws Exception {
            // Given
            Long taskId = 1L;
            EditTaskCommand editTaskCommand = EditTaskCommand.builder()
                    .estimatedTime(Duration.ofHours(33))
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(editTaskCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldFindAssignedMembers {

        @Test
        void shouldFindAssignedMembers() throws Exception {
            // Given
            Long taskId = 1L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}/members", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].username").value(hasItems("jaroslawPsikuta", "frneek")))
                    .andExpect(jsonPath("$[*].projectCount").value(hasItems(1, 3)))
                    .andExpect(jsonPath("$[*].commentsCount").value(hasItems(2, 3)))
                    .andExpect(jsonPath("$[*].tasksCount").value(hasItems(3, 15)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_ASSIGNED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_UPDATED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_DELETED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_DELETED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_CREATED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_CREATED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_ADDED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("projects", "tasks", "task-comments", "join-requests", "join-responses", "statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems("http://localhost/member/jaroslawPsikuta/projects", "http://localhost/member/jaroslawPsikuta/tasks", "http://localhost/member/jaroslawPsikuta/comments", "http://localhost/member/jaroslawPsikuta/join/requests", "http://localhost/member/jaroslawPsikuta/join/responses", "http://localhost/member/jaroslawPsikuta/statistics")))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("projects", "tasks", "task-comments", "join-requests", "join-responses", "statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems("http://localhost/member/frneek/projects", "http://localhost/member/frneek/tasks", "http://localhost/member/frneek/comments", "http://localhost/member/frneek/join/requests", "http://localhost/member/frneek/join/responses", "http://localhost/member/frneek/statistics")))
                    .andDo(print());
        }

    }

    @Nested
    class ShouldNotFindAssignedMembers {

        @Test
        void shouldNotFindAssignedMembersIfTaskDoesNotExist() throws Exception {
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
            mockMvc.perform(get("/task/{id}/members", taskId)
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
        void shouldNotFindAssignedMembersWithoutRoleHeader() throws Exception {
            // Given
            Long taskId = 1L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}/members", taskId)
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
        void shouldNotFindAssignedMembersWithoutUsernameHeader() throws Exception {
            // Given
            Long taskId = 1L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}/members", taskId)
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
    class ShouldAssignMembers {

        @Test
        void shouldAssignMembers() throws Exception {
            // Given
            Long taskId = 3L;
            String usernameToAssign = "jaroslawPsikuta";
            // When
            mockMvc.perform(get("/task/{id}/members", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].username").value(hasItems("frneek")))
                    .andExpect(jsonPath("$[*].projectCount").value(hasItems(3)))
                    .andExpect(jsonPath("$[*].commentsCount").value(hasItems(3)))
                    .andExpect(jsonPath("$[*].tasksCount").value(hasItems(15)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_ADDED").value(hasItems(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(hasItems(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_DELETED").value(hasItems(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_ASSIGNED").value(hasItems(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(hasItems(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_DELETED").value(hasItems(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_CREATED").value(hasItems(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(hasItems(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(hasItems(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_CREATED").value(hasItems(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_UPDATED").value(hasItems(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(hasItems(true)))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("projects", "tasks", "task-comments", "join-requests", "join-responses", "statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems("http://localhost/member/frneek/projects", "http://localhost/member/frneek/tasks",
                            "http://localhost/member/frneek/comments", "http://localhost/member/frneek/join/requests", "http://localhost/member/frneek/join/responses",
                            "http://localhost/member/frneek/statistics")))
                    .andDo(print());
            // Then
            mockMvc.perform(post("/task/{id}/members/assign", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .param("usernames", usernameToAssign))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].username").value("jaroslawPsikuta"))
                    .andExpect(jsonPath("$[0].projectCount").value(1))
                    .andExpect(jsonPath("$[0].commentsCount").value(2))
                    .andExpect(jsonPath("$[0].tasksCount").value(4))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$[0].links[0].rel").value("projects"))
                    .andExpect(jsonPath("$[0].links[0].href").value("http://localhost/member/jaroslawPsikuta/projects"))
                    .andExpect(jsonPath("$[0].links[1].rel").value("tasks"))
                    .andExpect(jsonPath("$[0].links[1].href").value("http://localhost/member/jaroslawPsikuta/tasks"))
                    .andExpect(jsonPath("$[0].links[2].rel").value("task-comments"))
                    .andExpect(jsonPath("$[0].links[2].href").value("http://localhost/member/jaroslawPsikuta/comments"))
                    .andExpect(jsonPath("$[0].links[3].rel").value("join-requests"))
                    .andExpect(jsonPath("$[0].links[3].href").value("http://localhost/member/jaroslawPsikuta/join/requests"))
                    .andExpect(jsonPath("$[0].links[4].rel").value("join-responses"))
                    .andExpect(jsonPath("$[0].links[4].href").value("http://localhost/member/jaroslawPsikuta/join/responses"))
                    .andExpect(jsonPath("$[0].links[5].rel").value("statistics"))
                    .andExpect(jsonPath("$[0].links[5].href").value("http://localhost/member/jaroslawPsikuta/statistics"))
                    .andDo(print());

            mockMvc.perform(get("/task/{id}/members", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].username").value(hasItems("jaroslawPsikuta", "frneek")))
                    .andExpect(jsonPath("$[*].projectCount").value(hasItems(1, 3)))
                    .andExpect(jsonPath("$[*].commentsCount").value(hasItems(2, 3)))
                    .andExpect(jsonPath("$[*].tasksCount").value(hasItems(4, 15)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_ADDED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_UPDATED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_DELETED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_DELETED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_ASSIGNED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_CREATED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_CREATED").value(hasItems(true, true)))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("projects", "tasks", "task-comments", "join-requests", "join-responses", "statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems("http://localhost/member/jaroslawPsikuta/projects", "http://localhost/member/jaroslawPsikuta/tasks", "http://localhost/member/jaroslawPsikuta/comments", "http://localhost/member/jaroslawPsikuta/join/requests", "http://localhost/member/jaroslawPsikuta/join/responses", "http://localhost/member/jaroslawPsikuta/statistics")))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("projects", "tasks", "task-comments", "join-requests", "join-responses", "statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems("http://localhost/member/frneek/projects", "http://localhost/member/frneek/tasks", "http://localhost/member/frneek/comments", "http://localhost/member/frneek/join/requests", "http://localhost/member/frneek/join/responses", "http://localhost/member/frneek/statistics")))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotAssignMembers {

        @Test
        void shouldNotAssignMembersIfTaskDoesNotExist() throws Exception {
            // Given
            Long nonExistingTaskId = 999L;
            String usernameToAssign = "jaroslawPsikuta";
            // When
            mockMvc.perform(get("/task/{id}", nonExistingTaskId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("TASK_NOT_FOUND"))
                    .andDo(print());
            // Then
            mockMvc.perform(post("/task/{id}/members/assign", nonExistingTaskId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .header(ROLE, "ADMIN")
                            .param("usernames", usernameToAssign))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("TASK_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotAssignMembersWithoutRoleHeader() throws Exception {
            // Given
            Long taskId = 1L;
            String usernameToAssign = "jaroslawPsikuta";
            // When
            // Then
            mockMvc.perform(post("/task/{id}/members/assign", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .param("usernames", usernameToAssign))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotAssignMembersWithoutUsernameHeader() throws Exception {
            // Given
            Long taskId = 1L;
            // When
            // Then
            mockMvc.perform(post("/task/{id}/members/assign", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .param("usernames", "jaroslawPsikuta"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }
}