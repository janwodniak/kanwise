package com.kanwise.kanwise_service.controller.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.kanwise_service.controller.DatabaseCleaner;
import com.kanwise.kanwise_service.model.member.command.CreateMemberCommand;
import com.kanwise.kanwise_service.model.member.command.EditMemberCommand;
import com.kanwise.kanwise_service.model.member.command.EditMemberPartiallyCommand;
import com.kanwise.kanwise_service.model.notification.ProjectNotificationType;
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
import java.util.EnumMap;
import java.util.Map;

import static com.kanwise.kanwise_service.model.http.HttpHeader.ROLE;
import static com.kanwise.kanwise_service.model.http.HttpHeader.USERNAME;
import static com.kanwise.kanwise_service.model.notification.ProjectNotificationType.PROJECT_ASSIGNED;
import static com.kanwise.kanwise_service.model.notification.ProjectNotificationType.PROJECT_CREATED;
import static com.kanwise.kanwise_service.model.notification.ProjectNotificationType.PROJECT_DELETED;
import static com.kanwise.kanwise_service.model.notification.ProjectNotificationType.PROJECT_JOIN_REQUEST_CREATED;
import static com.kanwise.kanwise_service.model.notification.ProjectNotificationType.PROJECT_MEMBER_ADDED;
import static com.kanwise.kanwise_service.model.notification.ProjectNotificationType.PROJECT_MEMBER_REMOVED;
import static com.kanwise.kanwise_service.model.notification.ProjectNotificationType.PROJECT_TASK_ASSIGNED;
import static com.kanwise.kanwise_service.model.notification.ProjectNotificationType.values;
import static java.lang.Boolean.FALSE;
import static java.time.ZonedDateTime.of;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;
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
class MemberControllerIT {

    private static final ZonedDateTime NOW = of(
            2022, 12, 21, 14, 0, 0, 0, ZoneId.of("UTC")
    );
    private final MockMvc mockMvc;
    private final DatabaseCleaner databaseCleaner;
    private final ObjectMapper objectMapper;
    @MockBean
    private Clock clock;

    @Autowired
    MemberControllerIT(MockMvc mockMvc, DatabaseCleaner databaseCleaner, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.databaseCleaner = databaseCleaner;
        this.objectMapper = objectMapper;
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
    class ShouldCreateMember {

        @Test
        void shouldCreateMember() throws Exception {
            // Given
            CreateMemberCommand createMemberCommand = new CreateMemberCommand("krzysztofJarzyna");
            // When
            // Then
            mockMvc.perform(post("/member")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createMemberCommand)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("krzysztofJarzyna"))
                    .andExpect(jsonPath("$.projectCount").value(0))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andExpect(jsonPath("$.tasksCount").value(0))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/krzysztofJarzyna/projects"))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/krzysztofJarzyna/tasks"))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/krzysztofJarzyna/comments"))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/krzysztofJarzyna/join/requests"))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/krzysztofJarzyna/join/responses"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/krzysztofJarzyna/statistics"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotCreateMember {

        @Test
        void shouldNotCreateMemberWithBlankUsername() throws Exception {
            // Given
            CreateMemberCommand createMemberCommand = new CreateMemberCommand(" ");
            // When
            // Then
            mockMvc.perform(post("/member")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createMemberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateMemberWithNotUniqueUsername() throws Exception {
            // Given
            String notUniqueUsername = "frneek";
            CreateMemberCommand createMemberCommand = new CreateMemberCommand(notUniqueUsername);
            // When
            mockMvc.perform(get("/member/{username}", notUniqueUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(notUniqueUsername))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(notUniqueUsername)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(notUniqueUsername)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(notUniqueUsername)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(notUniqueUsername)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(notUniqueUsername)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(notUniqueUsername)));
            // Then
            mockMvc.perform(post("/member")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createMemberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_UNIQUE')]").exists())
                    .andDo(print());
        }
    }

    @Nested
    class ShouldFindMember {
        @Test
        void shouldFindMember() throws Exception {
            // Given
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
        }
    }

    @Nested
    class ShouldNotFindMember {

        @Test
        void shouldNotFindMemberIfMemberDoesNotExists() throws Exception {
            // Given
            String nonExistingUsername = "nonExistingUsername";
            // When
            // Then
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
        }

        @Test
        void shouldNotFindMemberWithoutRoleHeader() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/member/frneek")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"));
        }

        @Test
        void shouldNotFindMemberWithoutUsernameHeader() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/member/frneek")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"));
        }
    }

    @Nested
    class ShouldEditMember {

        @Test
        void shouldEditMember() throws Exception {
            // Given
            String username = "frneek";
            String updatedUsername = "updatedUsername";

            EnumMap<ProjectNotificationType, Boolean> updatedNotificationSubscriptions = new EnumMap<>(ProjectNotificationType.class);
            for (ProjectNotificationType type : values()) {
                updatedNotificationSubscriptions.put(type, FALSE);
            }

            EditMemberCommand editMemberCommand = EditMemberCommand.builder()
                    .username(updatedUsername)
                    .notificationSubscriptions(updatedNotificationSubscriptions)
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
            // Then
            mockMvc.perform(put("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberCommand)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(updatedUsername))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(false))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(updatedUsername)));

            assertNotEquals(username, updatedUsername);
        }
    }

    @Nested
    class ShouldNotEditMember {

        @Test
        void shouldNotEditMemberIfMemberDoesNotExists() throws Exception {
            // Given
            String nonExistingUsername = "nonExistingUsername";
            String updatedUsername = "updatedUsername";

            EnumMap<ProjectNotificationType, Boolean> updatedNotificationSubscriptions = new EnumMap<>(ProjectNotificationType.class);
            for (ProjectNotificationType type : values()) {
                updatedNotificationSubscriptions.put(type, FALSE);
            }

            EditMemberCommand editMemberCommand = EditMemberCommand.builder()
                    .username(updatedUsername)
                    .notificationSubscriptions(updatedNotificationSubscriptions)
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
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
            // Then
            mockMvc.perform(put("/member/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotEditMemberWithBlankUsername() throws Exception {
            // Given
            String username = "frneek";
            String updatedUsername = " ";

            EnumMap<ProjectNotificationType, Boolean> updatedNotificationSubscriptions = new EnumMap<>(ProjectNotificationType.class);
            for (ProjectNotificationType type : values()) {
                updatedNotificationSubscriptions.put(type, FALSE);
            }

            EditMemberCommand editMemberCommand = EditMemberCommand.builder()
                    .username(updatedUsername)
                    .notificationSubscriptions(updatedNotificationSubscriptions)
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
            // Then
            mockMvc.perform(put("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberCommand)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_BLANK')]").exists())
                    .andDo(print());

            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));

            assertNotEquals(username, updatedUsername);
        }

        @Test
        void shouldNotEditMemberWithNotUniqueUsername() throws Exception {
            // Given
            String username = "frneek";
            String updatedUsername = "jaroslawPsikuta";

            EnumMap<ProjectNotificationType, Boolean> updatedNotificationSubscriptions = new EnumMap<>(ProjectNotificationType.class);
            for (ProjectNotificationType type : values()) {
                updatedNotificationSubscriptions.put(type, FALSE);
            }

            EditMemberCommand editMemberCommand = EditMemberCommand.builder()
                    .username(updatedUsername)
                    .notificationSubscriptions(updatedNotificationSubscriptions)
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));

            mockMvc.perform(get("/member/{username}", updatedUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk());
            // Then
            mockMvc.perform(put("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_UNIQUE')]").exists())
                    .andDo(print());

            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
        }

        @Test
        void shouldNotEditMemberWithNullSubscriptions() throws Exception {
            // Given
            String username = "frneek";
            EditMemberCommand editMemberCommand = new EditMemberCommand(username, null);
            // When
            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
            // Then
            mockMvc.perform(put("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberCommand)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'notificationSubscriptions' && @.message == 'NOTIFICATION_SUBSCRIPTIONS_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditMemberWithoutRoleHeader() throws Exception {
            // Given
            String username = "frneek";
            String updatedUsername = "jaroslawPsikuta";

            EnumMap<ProjectNotificationType, Boolean> updatedNotificationSubscriptions = new EnumMap<>(ProjectNotificationType.class);
            for (ProjectNotificationType type : values()) {
                updatedNotificationSubscriptions.put(type, FALSE);
            }

            EditMemberCommand editMemberCommand = EditMemberCommand.builder()
                    .username(updatedUsername)
                    .notificationSubscriptions(updatedNotificationSubscriptions)
                    .build();
            // When
            // Then
            mockMvc.perform(put("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotEditMemberWithoutUsernameHeader() throws Exception {
            // Given
            String username = "frneek";
            String updatedUsername = "jaroslawPsikuta";

            EnumMap<ProjectNotificationType, Boolean> updatedNotificationSubscriptions = new EnumMap<>(ProjectNotificationType.class);
            for (ProjectNotificationType type : values()) {
                updatedNotificationSubscriptions.put(type, FALSE);
            }

            EditMemberCommand editMemberCommand = EditMemberCommand.builder()
                    .username(updatedUsername)
                    .notificationSubscriptions(updatedNotificationSubscriptions)
                    .build();
            // When
            // Then
            mockMvc.perform(put("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADIMN")
                            .content(objectMapper.writeValueAsString(editMemberCommand)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldEditMemberPartially {

        @Test
        void shouldEditMemberPartially() throws Exception {
            // Given
            String username = "frneek";
            String updatedUsername = "krzsztofJarzyna";
            EnumMap<ProjectNotificationType, Boolean> updatedNotificationSubscriptions = new EnumMap<>(ProjectNotificationType.class);
            updatedNotificationSubscriptions.put(PROJECT_ASSIGNED, FALSE);
            EditMemberPartiallyCommand editMemberPartiallyCommand = EditMemberPartiallyCommand.builder()
                    .username(updatedUsername)
                    .notificationSubscriptions(updatedNotificationSubscriptions)
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
            // Then
            mockMvc.perform(patch("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberPartiallyCommand)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(updatedUsername))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(updatedUsername)));

            mockMvc.perform(get("/member/{username}", updatedUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(updatedUsername))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(updatedUsername)));
        }

        @Test
        void shouldEditMemberPartiallyUpdatingUsernameOnly() throws Exception {
            // Given
            String username = "frneek";
            String updatedUsername = "krzysztofJarzyna";
            EditMemberPartiallyCommand editMemberPartiallyCommand = EditMemberPartiallyCommand.builder()
                    .username(updatedUsername)
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));

            // Then
            mockMvc.perform(patch("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberPartiallyCommand)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(updatedUsername))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(updatedUsername)));

            mockMvc.perform(get("/member/{username}", updatedUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(updatedUsername))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(updatedUsername)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(updatedUsername)));
        }

        @Test
        void shouldEditMemberPartiallyUpdatingNotificationSubscriptionsOnly() throws Exception {
            // Given
            String username = "frneek";
            EnumMap<ProjectNotificationType, Boolean> notificationSubscriptions = new EnumMap<>(ProjectNotificationType.class);
            notificationSubscriptions.put(PROJECT_CREATED, false);
            notificationSubscriptions.put(PROJECT_DELETED, false);
            notificationSubscriptions.put(PROJECT_ASSIGNED, false);
            notificationSubscriptions.put(PROJECT_MEMBER_ADDED, false);

            EditMemberPartiallyCommand editMemberPartiallyCommand = EditMemberPartiallyCommand.builder()
                    .notificationSubscriptions(notificationSubscriptions)
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
            // Then
            mockMvc.perform(patch("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberPartiallyCommand)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));

            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(false))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
        }
    }

    @Nested
    class ShouldNotEditMemberPartially {

        @Test
        void shouldNotEditMemberPartiallyIfMemberDoesNotExists() throws Exception {
            // Given
            String nonExistingUsername = "nonExistingUsername";
            EditMemberPartiallyCommand editMemberPartiallyCommand = EditMemberPartiallyCommand.builder()
                    .notificationSubscriptions(Map.of(
                            PROJECT_MEMBER_REMOVED, true,
                            PROJECT_JOIN_REQUEST_CREATED, true,
                            PROJECT_TASK_ASSIGNED, true
                    ))
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
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/member/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberPartiallyCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotEditMemberPartiallyWithBlankUsername() throws Exception {
            // Given
            String username = "frneek";
            String blankUsername = "";

            EditMemberPartiallyCommand editMemberPartiallyCommand = EditMemberPartiallyCommand.builder()
                    .username(blankUsername)
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
            // Then
            mockMvc.perform(patch("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberPartiallyCommand)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NULL_OR_NOT_BLANK')]").exists())
                    .andDo(print());

            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
        }

        @Test
        void shouldNotEditMemberPartiallyWithNotUniqueUsername() throws Exception {
            // Given
            String username = "frneek";
            String notUniqueUsername = "jaroslawPsikuta";
            EditMemberPartiallyCommand editMemberPartiallyCommand = EditMemberPartiallyCommand.builder()
                    .username(notUniqueUsername)
                    .build();
            // When
            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));

            mockMvc.perform(get("/member/{username}", notUniqueUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(notUniqueUsername))
                    .andExpect(jsonPath("$.projectCount").value(1))
                    .andExpect(jsonPath("$.commentsCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(3))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(notUniqueUsername)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(notUniqueUsername)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(notUniqueUsername)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(notUniqueUsername)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(notUniqueUsername)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(notUniqueUsername)));
            // Then
            mockMvc.perform(patch("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberPartiallyCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'username' && @.message == 'USERNAME_NOT_UNIQUE')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditMemberPartiallyWithoutRoleHeader() throws Exception {
            // Given
            String username = "frneek";
            String updatedUsername = "frneek2";
            EditMemberPartiallyCommand editMemberPartiallyCommand = EditMemberPartiallyCommand.builder()
                    .username(updatedUsername)
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editMemberPartiallyCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotEditMemberPartiallyWithoutUsernameHeader() throws Exception {
            // Given
            String username = "frneek";
            String updatedUsername = "frneek2";
            EditMemberPartiallyCommand editMemberPartiallyCommand = EditMemberPartiallyCommand.builder()
                    .username(updatedUsername)
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(editMemberPartiallyCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldDeleteMember {

        @Test
        void shouldDeleteMember() throws Exception {
            // Given
            String username = "frneek";
            // When
            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
            // Then
            mockMvc.perform(delete("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"));
        }
    }

    @Nested
    class ShouldNotDeleteMember {

        @Test
        void shouldNotDeleteMemberIfMemberDoesNotExists() throws Exception {
            // Given
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
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"));
            // Then
            mockMvc.perform(delete("/member/{username}", nonExistingUsername)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"));
        }

        @Test
        void shouldNotDeleteMemberWithoutRoleHeader() throws Exception {
            // Given
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(delete("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"));
        }

        @Test
        void shouldNotDeleteMemberWithoutUsernameHeader() throws Exception {
            // Given
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(delete("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"));
        }
    }

    @Nested
    class ShouldFindProjectsForMember {

        @Test
        void shouldFindProjectsForMember() throws Exception {
            // Given
            String username = "frneek";
            // When
            mockMvc.perform(get("/member/{username}", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.projectCount").value(3))
                    .andExpect(jsonPath("$.commentsCount").value(3))
                    .andExpect(jsonPath("$.tasksCount").value(15))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                    .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                    .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                    .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                    .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
            // Then
            mockMvc.perform(get("/member/{username}/projects", username)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].id").value(hasItems(1, 2, 3)))
                    .andExpect(jsonPath("$[*].title").value(hasItems("Kanwise-Backend", "Kanwise-Frontend", "Kanwise-DevOps")))
                    .andExpect(jsonPath("$[*].description").exists())
                    .andExpect(jsonPath("$[*].createdAt").exists())
                    .andExpect(jsonPath("$[*].membersCount").value(hasItems(2, 1, 1)))
                    .andExpect(jsonPath("$[*].tasksCount").value(hasItems(5, 5, 5)))
                    .andExpect(jsonPath("$[*].todoTaskCount").value(hasItems(3, 2, 3)))
                    .andExpect(jsonPath("$[*].inProgressTaskCount").value(hasItems(1, 2, 2)))
                    .andExpect(jsonPath("$[*].doneTaskCount").value(hasItems(0, 0, 2)))
                    .andExpect(jsonPath("$[*].joinRequestsCount").value(hasItems(0, 0, 1)))
                    .andExpect(jsonPath("$[*].status").value(hasItems("CREATED", "ON_HOLD", "ON_TRACK")))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("project-members", "project-tasks", "project-statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems("http://localhost/project/1/members", "http://localhost/project/1/tasks", "http://localhost/project/1/statistics")))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("project-members", "project-tasks", "project-statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems("http://localhost/project/2/members", "http://localhost/project/2/tasks", "http://localhost/project/2/statistics")))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("project-members", "project-tasks", "project-statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems("http://localhost/project/3/members", "http://localhost/project/3/tasks", "http://localhost/project/3/statistics")))
                    .andDo(print());
        }

        @Nested
        class ShouldNotFindProjectsForMember {

            @Test
            void shouldNotFindProjectsForMemberIfMemberDoesNotExists() throws Exception {
                // Given
                String nonExistingUsername = "nonExistingUsername";
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
                        .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                        .andDo(print());
                // Then
                mockMvc.perform(get("/member/{username}/projects", nonExistingUsername)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN")
                                .header(USERNAME, "frneek"))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                        .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                        .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                        .andDo(print());
            }

            @Test
            void shouldNotFindProjectsForMemberWithoutRoleHeader() throws Exception {
                // Given
                String username = "frneek";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/projects", username)
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
            void shouldNotFindProjectsForMemberWithoutUsernameHeader() throws Exception {
                // Given
                String username = "frneek";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/projects", username)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN"))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                        .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                        .andDo(print());
            }
        }

        @Nested
        class ShouldFindTasksForMember {

            @Test
            void shouldFindTasksForMember() throws Exception {
                // Given
                String username = "jaroslawPsikuta";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/tasks", username)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN")
                                .header(USERNAME, "frneek"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$[*].taskId").value(hasItems(1, 4, 5)))
                        .andExpect(jsonPath("$[*].projectId").value(hasItems(1, 1, 1)))
                        .andExpect(jsonPath("$[*].authorUsername").value(hasItems("frneek", "frneek", "frneek")))
                        .andExpect(jsonPath("$[*].assignedMembersCount").value(hasItems(2, 2, 2)))
                        .andExpect(jsonPath("$[*].commentsCount").value(hasItems(0, 0, 0)))
                        .andExpect(jsonPath("$[*].statusesCount").value(hasItems(1, 1, 1)))
                        .andExpect(jsonPath("$[*].title").value(hasItems("Implement APIs for creating and updating kanban boards",
                                "Add integration with a notification service ",
                                "Add unit tests for the kanban board APIs ")))
                        .andExpect(jsonPath("$[*].description").value(hasItems("This task involves developing the APIs that allow users to create and update kanban boards in the application.",
                                "This task involves integrating the backend with a notification service to allow users to receive notifications in the application.",
                                "This task involves writing unit tests to ensure that the APIs for creating and updating kanban boards are working correctly.")))
                        .andExpect(jsonPath("$[*].priority").value(hasItems("NORMAL", "LOW", "HIGH")))
                        .andExpect(jsonPath("$[*].type").value(hasItems("NEW_FEATURE", "NEW_FEATURE", "TEST")))
                        .andExpect(jsonPath("$[*].estimatedTime").value(hasItems(86400.000000000, 21600.000000000, 21600.000000000)))
                        .andExpect(jsonPath("$[*].currentStatus").value(hasItems("TODO", "TODO", "TODO")))
                        .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("project", "author", "assigned-members", "statistics", "comments", "statuses")))
                        .andExpect(jsonPath("$[*].links[*].href").value(hasItems("http://localhost/project/1", "http://localhost/member/frneek", "http://localhost/task/1/members", "http://localhost/task/1/statistics", "http://localhost/task/1/comments", "http://localhost/task/1/statuses")))
                        .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("project", "author", "assigned-members", "statistics", "comments", "statuses")))
                        .andExpect(jsonPath("$[*].links[*].href").value(hasItems("http://localhost/project/1", "http://localhost/member/frneek", "http://localhost/task/4/members", "http://localhost/task/4/statistics", "http://localhost/task/4/comments", "http://localhost/task/4/statuses")))
                        .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("project", "author", "assigned-members", "statistics", "comments", "statuses")))
                        .andExpect(jsonPath("$[*].links[*].href").value(hasItems("http://localhost/project/1", "http://localhost/member/frneek", "http://localhost/task/5/members", "http://localhost/task/5/statistics", "http://localhost/task/5/comments", "http://localhost/task/5/statuses")))
                        .andDo(print());
            }
        }

        @Nested
        class ShouldNotFindTasksForMember {

            @Test
            void shouldNotFindTasksForMemberIfMemberDoesNotExists() throws Exception {
                // Given
                String username = "notExistingUsername";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/tasks", username)
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
            void shouldNotFindTasksForMemberWithoutRoleHeader() throws Exception {
                // Given
                // When
                // Then
                mockMvc.perform(get("/member/frneek/tasks")
                                .contentType(APPLICATION_JSON)
                                .header(USERNAME, "frneek"))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                        .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                        .andDo(print());
            }

            @Test
            void shouldNotFindTasksForMemberWithoutUsernameHeader() throws Exception {
                // Given
                // When
                // Then
                mockMvc.perform(get("/member/frneek/tasks")
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN"))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                        .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                        .andDo(print());
            }
        }

        @Nested
        class ShouldFindTaskCommentsForMember {

            @Test
            void shouldFindTaskCommentsForMember() throws Exception {
                // Given
                String username = "frneek";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/comments", username)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN")
                                .header(USERNAME, "frneek"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(3)))
                        .andExpect(jsonPath("$[*].id", containsInAnyOrder(5, 1, 3)))
                        .andExpect(jsonPath("$[*].authorUsername", containsInAnyOrder("frneek", "frneek", "frneek")))
                        .andExpect(jsonPath("$[*].taskId", containsInAnyOrder(3, 3, 3)))
                        .andExpect(jsonPath("$[*].content", containsInAnyOrder("Definitely. I think it's a reliable and efficient way to handle database migrations, and it saves us a lot of time and effort. Let's make sure to use it for any future migrations we need to do.", "Hey, I just wanted to check in about the database migration script we implemented using Liquibase. How did it go?", "That's great to hear! I'm glad we decided to use Liquibase for this task. It made it much easier to manage the database migration and handle any changes or updates we needed to make.")))
                        .andExpect(jsonPath("$[*].commentedAt").exists())
                        .andExpect(jsonPath("$[*].likesCount", containsInAnyOrder(0, 0, 1)))
                        .andExpect(jsonPath("$[*].dislikesCount", containsInAnyOrder(0, 0, 0)))
                        .andExpect(jsonPath("$[*].links[*].rel", containsInAnyOrder("task-comment-author", "task", "assigned-members", "statistics", "comments", "statuses", "task-comment-author", "task", "assigned-members", "statistics", "comments", "statuses", "task-comment-author", "task", "assigned-members", "statistics", "comments", "statuses")))
                        .andExpect(jsonPath("$[*].links[*].href", containsInAnyOrder("http://localhost/member/frneek", "http://localhost/task/3", "http://localhost/task/3/members", "http://localhost/task/3/statistics", "http://localhost/task/3/comments", "http://localhost/task/3/statuses", "http://localhost/member/frneek", "http://localhost/task/3", "http://localhost/task/3/members", "http://localhost/task/3/statistics", "http://localhost/task/3/comments", "http://localhost/task/3/statuses", "http://localhost/member/frneek", "http://localhost/task/3", "http://localhost/task/3/members", "http://localhost/task/3/statistics", "http://localhost/task/3/comments", "http://localhost/task/3/statuses")))
                        .andDo(print());
            }
        }

        @Nested
        class ShouldNotFindTaskCommentsForMember {

            @Test
            void shouldNotFindTaskCommentsForMemberIfMemberDoesNotExists() throws Exception {
                // Given
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
                mockMvc.perform(get("/member/{username}/comments", nonExistingUsername)
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
            void shouldNotFindTaskCommentsWithoutRoleHeader() throws Exception {
                // Given
                String username = "frneek";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/comments", username)
                                .contentType(APPLICATION_JSON)
                                .header(USERNAME, "frneek"))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                        .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                        .andDo(print());
            }

            @Test
            void shouldNotFindTaskCommentsWithoutUsernameHeader() throws Exception {
                // Given
                String username = "frneek";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/comments", username)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN"))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                        .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                        .andDo(print());
            }
        }

        @Nested
        class ShouldFindStatisticsForMember {

            @Test
            void shouldFindStatisticsForMember() throws Exception {
                // Given
                String username = "frneek";
                // When
                mockMvc.perform(get("/member/{username}", username)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN")
                                .header(USERNAME, "frneek"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value(username))
                        .andExpect(jsonPath("$.projectCount").value(3))
                        .andExpect(jsonPath("$.commentsCount").value(3))
                        .andExpect(jsonPath("$.tasksCount").value(15))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                        .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                        .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                        .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                        .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                        .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                        .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
                // Then
                mockMvc.perform(get("/member/{username}/statistics", username)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN")
                                .header(USERNAME, "frneek"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.memberUsername").value("frneek"))
                        .andExpect(jsonPath("$.totalTasksCount").value(15))
                        .andExpect(jsonPath("$.performancePercentage").value(0))
                        .andExpect(jsonPath("$.totalEstimatedTime").value("PT145H"))
                        .andExpect(jsonPath("$.totalTasksStatusCountMap.TODO").value(8))
                        .andExpect(jsonPath("$.totalTasksStatusCountMap.IN_PROGRESS").value(5))
                        .andExpect(jsonPath("$.totalTasksStatusCountMap.RESOLVED").value(2))
                        .andExpect(jsonPath("$.totalTasksTypeCountMap.NEW_FEATURE").value(9))
                        .andExpect(jsonPath("$.totalTasksTypeCountMap.IMPROVEMENT").value(2))
                        .andExpect(jsonPath("$.totalTasksTypeCountMap.TEST").value(2))
                        .andExpect(jsonPath("$.totalTasksTypeCountMap.DOCUMENTATION").value(1))
                        .andExpect(jsonPath("$.totalTasksStatusCountByProjectMap.Kanwise-DevOps.TODO").value(3))
                        .andExpect(jsonPath("$.totalTasksStatusCountByProjectMap.Kanwise-DevOps.IN_PROGRESS").value(2))
                        .andExpect(jsonPath("$.totalTasksStatusCountByProjectMap.Kanwise-Frontend.TODO").value(2))
                        .andExpect(jsonPath("$.totalTasksStatusCountByProjectMap.Kanwise-Frontend.IN_PROGRESS").value(2))
                        .andExpect(jsonPath("$.totalTasksStatusCountByProjectMap.Kanwise-Backend.TODO").value(3))
                        .andExpect(jsonPath("$.totalTasksStatusCountByProjectMap.Kanwise-Backend.IN_PROGRESS").value(1))
                        .andExpect(jsonPath("$.totalTasksStatusCountByProjectMap.Kanwise-Backend.RESOLVED").value(2))
                        .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/frneek/projects"))
                        .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/frneek/tasks"))
                        .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/frneek/comments"))
                        .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/frneek/join/requests"))
                        .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/frneek/join/responses"))
                        .andDo(print());
            }
        }

        @Nested
        class ShouldNotFindStatisticsForMember {

            @Test
            void shouldNotFindStatisticsForMemberIfMemberDoesNotExists() throws Exception {
                // Given
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
                        .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"));
                // Then
                mockMvc.perform(get("/member/{username}/statistics", nonExistingUsername)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN")
                                .header(USERNAME, "frneek"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                        .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                        .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"));
            }


            @Test
            void shouldNotFindStatisticsForMemberWithoutRoleHeader() throws Exception {
                // Given
                String username = "frneek";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/statistics", username)
                                .contentType(APPLICATION_JSON)
                                .header(USERNAME, "frneek"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                        .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"));
            }

            @Test
            void shouldNotFindStatusesForMemberWithoutUsernameHeader() throws Exception {
                // Given
                String username = "frneek";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/statistics", username)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.timestamp").exists())
                        .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                        .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"));
            }
        }

        @Nested
        class ShouldFindJoinRequestsForMember {

            @Test
            void shouldFindJoinRequestsForMember() throws Exception {
                // Given
                String username = "jaroslawPsikuta";
                // When
                mockMvc.perform(get("/member/{username}", username)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN")
                                .header(USERNAME, "frneek"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value(username))
                        .andExpect(jsonPath("$.projectCount").value(1))
                        .andExpect(jsonPath("$.commentsCount").value(2))
                        .andExpect(jsonPath("$.tasksCount").value(3))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                        .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                        .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                        .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                        .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                        .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                        .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
                // Then
                mockMvc.perform(get("/member/{username}/join/requests", username)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN")
                                .header(USERNAME, "frneek"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("[*].id").value(hasItems(1, 2, 3)))
                        .andExpect(jsonPath("[*].projectId").value(hasItems(1, 2, 3)))
                        .andExpect(jsonPath("[*].requestedByUsername").value(hasItems("jaroslawPsikuta", "jaroslawPsikuta", "jaroslawPsikuta")))
                        .andExpect(jsonPath("[*].requestedAt").exists())
                        .andExpect(jsonPath("[*].message").value(hasItems("Hi, I am a devops engineer interested in joining the Kanwise-Devops project. I have experience with CI/CD pipelines and automating builds and deployments. Please let me know if you have any openings or if you'd like to discuss further.",
                                "Hi team, I am a software developer interested in joining the Kanwise-Backend project. I have experience with Java, Spring Boot, and RESTful APIs. Please let me know if you have any openings.",
                                "Hello, I'm a frontend developer interested in joining the Kanwise-Frontend project. I have experience with modern frontend frameworks and responsive design. Please let me know if you have any openings or if you'd like to discuss further.")))
                        .andExpect(jsonPath("[*].links[*].rel").value(hasItems("requested-by", "requested-by", "requested-by", "project", "project", "project", "responded-by", "responded-by", "responded-by")))
                        .andExpect(jsonPath("[*].links[*].href").value(hasItems("http://localhost/member/jaroslawPsikuta", "http://localhost/member/jaroslawPsikuta", "http://localhost/member/jaroslawPsikuta", "http://localhost/project/3", "http://localhost/project/1", "http://localhost/project/2", "http://localhost/member/frneek", "http://localhost/member/frneek", "http://localhost/member/frneek")))
                        .andDo(print());
            }
        }


        @Nested
        class ShouldNotFindJoinRequestsForMember {

            @Test
            void shouldNotFindJoinRequestsForMemberIfMemberDoesNotExists() throws Exception {
                // Given
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
                mockMvc.perform(get("/member/{username}/join/requests", nonExistingUsername)
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
            void shouldNotFindJoinRequestsForMemberWithoutRoleHeader() throws Exception {
                // Given
                String username = "frneek";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/join/responses", username)
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
            void shouldNotFindJoinRequestsForMemberWithoutUsernameHeader() throws Exception {
                // Given
                String username = "frneek";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/join/responses", username)
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
        class ShouldFindJoinResponsesForMember {

            @Test
            void shouldFindJoinResponsesForMember() throws Exception {
                // Given
                String username = "jaroslawPsikuta";
                // When
                mockMvc.perform(get("/member/{username}", username)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN")
                                .header(USERNAME, "frneek"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value(username))
                        .andExpect(jsonPath("$.projectCount").value(1))
                        .andExpect(jsonPath("$.commentsCount").value(2))
                        .andExpect(jsonPath("$.tasksCount").value(3))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_CREATED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_DELETED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                        .andExpect(jsonPath("$.notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                        .andExpect(jsonPath("$._links.projects.href").value("http://localhost/member/%s/projects".formatted(username)))
                        .andExpect(jsonPath("$._links.tasks.href").value("http://localhost/member/%s/tasks".formatted(username)))
                        .andExpect(jsonPath("$._links.task-comments.href").value("http://localhost/member/%s/comments".formatted(username)))
                        .andExpect(jsonPath("$._links.join-requests.href").value("http://localhost/member/%s/join/requests".formatted(username)))
                        .andExpect(jsonPath("$._links.join-responses.href").value("http://localhost/member/%s/join/responses".formatted(username)))
                        .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/member/%s/statistics".formatted(username)));
                // Then
                mockMvc.perform(get("/member/{username}/join/responses", username)
                                .contentType(APPLICATION_JSON)
                                .header(ROLE, "ADMIN")
                                .header(USERNAME, "frneek"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("[*].id").value(hasItems(1, 2)))
                        .andExpect(jsonPath("[*].respondedByUsername").value(hasItems("frneek", "frneek")))
                        .andExpect(jsonPath("[*].joinRequestId").value(hasItems(1, 2)))
                        .andExpect(jsonPath("[*].status").value(hasItems("ACCEPTED", "REJECTED")))
                        .andExpect(jsonPath("[*].respondedAt").exists())
                        .andExpect(jsonPath("[*].message").value(hasItems("Hi Jarosław! Thank you for your interest in joining the Kanwise-Backend project. We are pleased to offer you a position on the team. Please let us know if you have any questions or if there is anything we can do to support you as you get started.",
                                "Hi Jarosław! Thank you for your interest in joining the Kanwise-Frontend project. Unfortunately, we are unable to offer you a position on the team at this time. We appreciate your interest and encourage you to keep an eye out for future opportunities.")))
                        .andExpect(jsonPath("[*].links[*].rel").value(hasItems("responded-by", "responded-by", "join-request", "join-request")))
                        .andExpect(jsonPath("[*].links[*].href").value(hasItems("http://localhost/member/frneek", "http://localhost/member/frneek", "http://localhost/join/request/1", "http://localhost/join/request/2")))
                        .andDo(print());
            }

        }

        @Nested
        class ShouldNotFindJoinResponsesForMember {

            @Test
            void shouldNotFindJoinResponsesForMemberIfMemberDoesNotExists() throws Exception {
                // Given
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
                mockMvc.perform(get("/member/{username}/join/responses", nonExistingUsername)
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
            void shouldNotFindJoinResponsesForMemberWithoutRoleHeader() throws Exception {
                // Given
                String username = "frneek";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/join/responses", username)
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
            void shouldNotFindJoinResponsesForMemberWithoutUsernameHeader() throws Exception {
                // Given
                String username = "frneek";
                // When
                // Then
                mockMvc.perform(get("/member/{username}/join/responses", username)
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
}