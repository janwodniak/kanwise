package com.kanwise.kanwise_service.controller.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.kanwise_service.controller.DatabaseCleaner;
import com.kanwise.kanwise_service.model.task_comment.command.CreateTaskCommentCommand;
import com.kanwise.kanwise_service.model.task_comment_reaction.command.CreateTaskCommentReactionCommand;
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
class TaskCommentControllerIT {

    private final MockMvc mockMvc;
    private final DatabaseCleaner databaseCleaner;
    private final ObjectMapper objectMapper;

    @Autowired
    TaskCommentControllerIT(MockMvc mockMvc, DatabaseCleaner databaseCleaner, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.databaseCleaner = databaseCleaner;
        this.objectMapper = objectMapper;
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }

    @Nested
    class ShouldCreateTaskComment {

        @Test
        void shouldCreateTaskComment() throws Exception {
            // Given
            Long taskId = 1L;
            CreateTaskCommentCommand createTaskCommentCommand = CreateTaskCommentCommand.builder()
                    .taskId(taskId)
                    .authorUsername("frneek")
                    .content("content")
                    .build();
            // When
            mockMvc.perform(get("/task/{id}/comments", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));

            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.commentsCount").value(0))
                    .andDo(print());
            // Then
            mockMvc.perform(post("/task/comment")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentCommand)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(6))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.taskId").value(1))
                    .andExpect(jsonPath("$.content").value("content"))
                    .andExpect(jsonPath("$.commentedAt").isNotEmpty())
                    .andExpect(jsonPath("$.likesCount").value(0))
                    .andExpect(jsonPath("$.dislikesCount").value(0))
                    .andExpect(jsonPath("$._links.task-comment-author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.task.href").value("http://localhost/task/1"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/1/statuses"))
                    .andDo(print());

            mockMvc.perform(get("/task/{id}/comments", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(6))
                    .andExpect(jsonPath("$.content[0].authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.content[0].taskId").value(1))
                    .andExpect(jsonPath("$.content[0].content").value("content"))
                    .andExpect(jsonPath("$.content[0].commentedAt").isNotEmpty())
                    .andExpect(jsonPath("$.content[0].likesCount").value(0))
                    .andExpect(jsonPath("$.content[0].dislikesCount").value(0))
                    .andExpect(jsonPath("$.content[0].links[0].rel").value("task-comment-author"))
                    .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$.content[0].links[1].rel").value("task"))
                    .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/task/1"))
                    .andExpect(jsonPath("$.content[0].links[2].rel").value("assigned-members"))
                    .andExpect(jsonPath("$.content[0].links[2].href").value("http://localhost/task/1/members"))
                    .andExpect(jsonPath("$.content[0].links[3].rel").value("statistics"))
                    .andExpect(jsonPath("$.content[0].links[3].href").value("http://localhost/task/1/statistics"))
                    .andExpect(jsonPath("$.content[0].links[4].rel").value("comments"))
                    .andExpect(jsonPath("$.content[0].links[4].href").value("http://localhost/task/1/comments"))
                    .andExpect(jsonPath("$.content[0].links[5].rel").value("statuses"))
                    .andExpect(jsonPath("$.content[0].links[5].href").value("http://localhost/task/1/statuses"))
                    .andDo(print());

            mockMvc.perform(get("/task/{id}", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskId").value(taskId))
                    .andExpect(jsonPath("$.commentsCount").value(1))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotCreateTaskComment {

        @Test
        void shouldNotCreateTaskCommentWithNullTaskId() throws Exception {
            // Given
            CreateTaskCommentCommand createTaskCommentCommand = CreateTaskCommentCommand.builder()
                    .taskId(null)
                    .authorUsername("frneek")
                    .content("content")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'taskId' && @.message == 'TASK_ID_NOT_NULL')]").exists())
                    .andDo(print());

        }

        @Test
        void shouldNotCreateTaskCommentIfTaskDoesNotExist() throws Exception {
            // Given
            Long taskId = 999L;
            CreateTaskCommentCommand createTaskCommentCommand = CreateTaskCommentCommand.builder()
                    .taskId(taskId)
                    .authorUsername("frneek")
                    .content("content")
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
            mockMvc.perform(post("/task/comment")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("TASK_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskCommentWithNullContent() throws Exception {
            // Given
            CreateTaskCommentCommand createTaskCommentCommand = CreateTaskCommentCommand.builder()
                    .taskId(1L)
                    .authorUsername("frneek")
                    .content(null)
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'content' && @.message == 'CONTENT_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskCommentWithBlankContent() throws Exception {
            // Given
            Long taskId = 1L;
            CreateTaskCommentCommand createTaskCommentCommand = CreateTaskCommentCommand.builder()
                    .taskId(taskId)
                    .authorUsername("frneek")
                    .content(" ")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'content' && @.message == 'CONTENT_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskCommentWithNullAuthorUsername() throws Exception {
            // Given
            CreateTaskCommentCommand createTaskCommentCommand = CreateTaskCommentCommand.builder()
                    .taskId(1L)
                    .authorUsername(null)
                    .content("content")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'authorUsername' && @.message == 'AUTHOR_USERNAME_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskCommentIfAuthorDoesNotExist() throws Exception {
            // Given
            String nonExistingUsername = "nonExistingUsername";
            CreateTaskCommentCommand createTaskCommentCommand = CreateTaskCommentCommand.builder()
                    .taskId(1L)
                    .authorUsername(nonExistingUsername)
                    .content("content")
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
            mockMvc.perform(post("/task/comment")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskCommentWithoutRoleHeader() throws Exception {
            // Given
            CreateTaskCommentCommand createTaskCommentCommand = CreateTaskCommentCommand.builder()
                    .taskId(1L)
                    .authorUsername("frneek")
                    .content("content")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskCommentWithoutUsernameHeader() throws Exception {
            // Given
            CreateTaskCommentCommand createTaskCommentCommand = CreateTaskCommentCommand.builder()
                    .taskId(1L)
                    .authorUsername("frneek")
                    .content("content")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(createTaskCommentCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }


    @Nested
    class ShouldFindTaskCommentsForTask {

        @Test
        void shouldFindTaskCommentsForTask() throws Exception {
            // Given
            Long taskId = 3L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}/comments", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.content[0].taskId").value(3))
                    .andExpect(jsonPath("$.content[0].content").value("Hey, I just wanted to check in about the database migration script we implemented using Liquibase. How did it go?"))
                    .andExpect(jsonPath("$.content[0].commentedAt").exists())
                    .andExpect(jsonPath("$.content[0].likesCount").value(0))
                    .andExpect(jsonPath("$.content[0].dislikesCount").value(0))
                    .andExpect(jsonPath("$.content[0].links[0].rel").value("task-comment-author"))
                    .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$.content[0].links[1].rel").value("task"))
                    .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/task/3"))
                    .andExpect(jsonPath("$.content[0].links[2].rel").value("assigned-members"))
                    .andExpect(jsonPath("$.content[0].links[2].href").value("http://localhost/task/3/members"))
                    .andExpect(jsonPath("$.content[0].links[3].rel").value("statistics"))
                    .andExpect(jsonPath("$.content[0].links[3].href").value("http://localhost/task/3/statistics"))
                    .andExpect(jsonPath("$.content[0].links[4].rel").value("comments"))
                    .andExpect(jsonPath("$.content[0].links[4].href").value("http://localhost/task/3/comments"))
                    .andExpect(jsonPath("$.content[0].links[5].rel").value("statuses"))
                    .andExpect(jsonPath("$.content[0].links[5].href").value("http://localhost/task/3/statuses"))
                    .andExpect(jsonPath("$.content[1].id").value(2))
                    .andExpect(jsonPath("$.content[1].authorUsername").value("jaroslawPsikuta"))
                    .andExpect(jsonPath("$.content[1].taskId").value(3))
                    .andExpect(jsonPath("$.content[1].content").value("It went great! The script worked perfectly and we were able to migrate the database to the new version without any issues."))
                    .andExpect(jsonPath("$.content[1].commentedAt").exists())
                    .andExpect(jsonPath("$.content[1].likesCount").value(1))
                    .andExpect(jsonPath("$.content[1].dislikesCount").value(0))
                    .andExpect(jsonPath("$.content[1].links[0].rel").value("task-comment-author"))
                    .andExpect(jsonPath("$.content[1].links[0].href").value("http://localhost/member/jaroslawPsikuta"))
                    .andExpect(jsonPath("$.content[1].links[1].rel").value("task"))
                    .andExpect(jsonPath("$.content[1].links[1].href").value("http://localhost/task/3"))
                    .andExpect(jsonPath("$.content[1].links[2].rel").value("assigned-members"))
                    .andExpect(jsonPath("$.content[1].links[2].href").value("http://localhost/task/3/members"))
                    .andExpect(jsonPath("$.content[1].links[3].rel").value("statistics"))
                    .andExpect(jsonPath("$.content[1].links[3].href").value("http://localhost/task/3/statistics"))
                    .andExpect(jsonPath("$.content[1].links[4].rel").value("comments"))
                    .andExpect(jsonPath("$.content[1].links[4].href").value("http://localhost/task/3/comments"))
                    .andExpect(jsonPath("$.content[1].links[5].rel").value("statuses"))
                    .andExpect(jsonPath("$.content[1].links[5].href").value("http://localhost/task/3/statuses"))
                    .andExpect(jsonPath("$.content[2].id").value(3))
                    .andExpect(jsonPath("$.content[2].authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.content[2].taskId").value(3))
                    .andExpect(jsonPath("$.content[2].content").value("That's great to hear! I'm glad we decided to use Liquibase for this task. It made it much easier to manage the database migration and handle any changes or updates we needed to make."))
                    .andExpect(jsonPath("$.content[2].commentedAt").exists())
                    .andExpect(jsonPath("$.content[2].likesCount").value(1))
                    .andExpect(jsonPath("$.content[2].dislikesCount").value(0))
                    .andExpect(jsonPath("$.content[2].links[0].rel").value("task-comment-author"))
                    .andExpect(jsonPath("$.content[2].links[0].href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$.content[2].links[1].rel").value("task"))
                    .andExpect(jsonPath("$.content[2].links[1].href").value("http://localhost/task/3"))
                    .andExpect(jsonPath("$.content[2].links[2].rel").value("assigned-members"))
                    .andExpect(jsonPath("$.content[2].links[2].href").value("http://localhost/task/3/members"))
                    .andExpect(jsonPath("$.content[2].links[3].rel").value("statistics"))
                    .andExpect(jsonPath("$.content[2].links[3].href").value("http://localhost/task/3/statistics"))
                    .andExpect(jsonPath("$.content[2].links[4].rel").value("comments"))
                    .andExpect(jsonPath("$.content[2].links[4].href").value("http://localhost/task/3/comments"))
                    .andExpect(jsonPath("$.content[2].links[5].rel").value("statuses"))
                    .andExpect(jsonPath("$.content[2].links[5].href").value("http://localhost/task/3/statuses"))
                    .andExpect(jsonPath("$.content[3].id").value(4))
                    .andExpect(jsonPath("$.content[3].authorUsername").value("jaroslawPsikuta"))
                    .andExpect(jsonPath("$.content[3].taskId").value(3))
                    .andExpect(jsonPath("$.content[3].content").value("Yeah, definitely. It's a really useful tool and I'm glad we have it in our toolkit. Do you think we should use it for future database migrations as well?"))
                    .andExpect(jsonPath("$.content[3].commentedAt").exists())
                    .andExpect(jsonPath("$.content[3].likesCount").value(0))
                    .andExpect(jsonPath("$.content[3].dislikesCount").value(0))
                    .andExpect(jsonPath("$.content[3].links[0].rel").value("task-comment-author"))
                    .andExpect(jsonPath("$.content[3].links[0].href").value("http://localhost/member/jaroslawPsikuta"))
                    .andExpect(jsonPath("$.content[3].links[1].rel").value("task"))
                    .andExpect(jsonPath("$.content[3].links[1].href").value("http://localhost/task/3"))
                    .andExpect(jsonPath("$.content[3].links[2].rel").value("assigned-members"))
                    .andExpect(jsonPath("$.content[3].links[2].href").value("http://localhost/task/3/members"))
                    .andExpect(jsonPath("$.content[3].links[3].rel").value("statistics"))
                    .andExpect(jsonPath("$.content[3].links[3].href").value("http://localhost/task/3/statistics"))
                    .andExpect(jsonPath("$.content[3].links[4].rel").value("comments"))
                    .andExpect(jsonPath("$.content[3].links[4].href").value("http://localhost/task/3/comments"))
                    .andExpect(jsonPath("$.content[3].links[5].rel").value("statuses"))
                    .andExpect(jsonPath("$.content[3].links[5].href").value("http://localhost/task/3/statuses"))
                    .andExpect(jsonPath("$.content[4].id").value(5))
                    .andExpect(jsonPath("$.content[4].authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.content[4].taskId").value(3))
                    .andExpect(jsonPath("$.content[4].content").value("Definitely. I think it's a reliable and efficient way to handle database migrations, and it saves us a lot of time and effort. Let's make sure to use it for any future migrations we need to do."))
                    .andExpect(jsonPath("$.content[4].commentedAt").exists())
                    .andExpect(jsonPath("$.content[4].likesCount").value(0))
                    .andExpect(jsonPath("$.content[4].dislikesCount").value(0))
                    .andExpect(jsonPath("$.content[4].links[0].rel").value("task-comment-author"))
                    .andExpect(jsonPath("$.content[4].links[0].href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$.content[4].links[1].rel").value("task"))
                    .andExpect(jsonPath("$.content[4].links[1].href").value("http://localhost/task/3"))
                    .andExpect(jsonPath("$.content[4].links[2].rel").value("assigned-members"))
                    .andExpect(jsonPath("$.content[4].links[2].href").value("http://localhost/task/3/members"))
                    .andExpect(jsonPath("$.content[4].links[3].rel").value("statistics"))
                    .andExpect(jsonPath("$.content[4].links[3].href").value("http://localhost/task/3/statistics"))
                    .andExpect(jsonPath("$.content[4].links[4].rel").value("comments"))
                    .andExpect(jsonPath("$.content[4].links[4].href").value("http://localhost/task/3/comments"))
                    .andExpect(jsonPath("$.content[4].links[5].rel").value("statuses"))
                    .andExpect(jsonPath("$.content[4].links[5].href").value("http://localhost/task/3/statuses"))
                    .andExpect(jsonPath("$.pageable.sort.empty").value(false))
                    .andExpect(jsonPath("$.pageable.sort.unsorted").value(false))
                    .andExpect(jsonPath("$.pageable.sort.sorted").value(true))
                    .andExpect(jsonPath("$.pageable.offset").value(0))
                    .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                    .andExpect(jsonPath("$.pageable.pageSize").value(5))
                    .andExpect(jsonPath("$.pageable.paged").value(true))
                    .andExpect(jsonPath("$.pageable.unpaged").value(false))
                    .andExpect(jsonPath("$.last").value(true))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(5))
                    .andExpect(jsonPath("$.first").value(true))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.sort.empty").value(false))
                    .andExpect(jsonPath("$.sort.unsorted").value(false))
                    .andExpect(jsonPath("$.sort.sorted").value(true))
                    .andExpect(jsonPath("$.numberOfElements").value(5))
                    .andExpect(jsonPath("$.empty").value(false))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotFindTaskCommentsForTask {

        @Test
        void shouldNotFindTaskCommentsForTaskWithoutRoleHeader() throws Exception {
            // Given
            Long taskId = 1L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}/comments", taskId)
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
        void shouldNotFindTaskCommentsForTaskWithoutUsernameHeader() throws Exception {
            // Given
            Long taskId = 1L;
            // When
            // Then
            mockMvc.perform(get("/task/{id}/comments", taskId)
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
    class ShouldCreateTaskCommentReaction {

        @Test
        void shouldCreateLikeTaskCommentReaction() throws Exception {
            // Given
            CreateTaskCommentReactionCommand createTaskCommentReactionCommand = CreateTaskCommentReactionCommand.builder()
                    .commentId(1L)
                    .authorUsername("frneek")
                    .reactionLabel("LIKE")
                    .build();
            // When

            // Then
            mockMvc.perform(post("/task/comment/reaction")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentReactionCommand)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.taskId").value(3))
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.reactionLabel").value("LIKE"))
                    .andExpect(jsonPath("$.reactedAt").exists())
                    .andExpect(jsonPath("$._links.task-comment-author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.task-reaction-author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.task.href").value("http://localhost/task/3"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/3/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/3/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/3/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/3/statuses"))
                    .andDo(print());
        }

        @Test
        void shouldCreateDislikeTaskCommentReaction() throws Exception {
            // Given
            CreateTaskCommentReactionCommand createTaskCommentReactionCommand = CreateTaskCommentReactionCommand.builder()
                    .commentId(1L)
                    .authorUsername("frneek")
                    .reactionLabel("DISLIKE")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment/reaction")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentReactionCommand)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.taskId").value(3))
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.authorUsername").value("frneek"))
                    .andExpect(jsonPath("$.reactionLabel").value("DISLIKE"))
                    .andExpect(jsonPath("$.reactedAt").exists())
                    .andExpect(jsonPath("$._links.task-comment-author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.task-reaction-author.href").value("http://localhost/member/frneek"))
                    .andExpect(jsonPath("$._links.task.href").value("http://localhost/task/3"))
                    .andExpect(jsonPath("$._links.assigned-members.href").value("http://localhost/task/3/members"))
                    .andExpect(jsonPath("$._links.statistics.href").value("http://localhost/task/3/statistics"))
                    .andExpect(jsonPath("$._links.comments.href").value("http://localhost/task/3/comments"))
                    .andExpect(jsonPath("$._links.statuses.href").value("http://localhost/task/3/statuses"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotCreateTaskCommentReaction {

        @Test
        void shouldNotCreateTaskCommentReactionWithNullTaskCommentId() throws Exception {
            // Given
            CreateTaskCommentReactionCommand createTaskCommentReactionCommand = CreateTaskCommentReactionCommand.builder()
                    .commentId(null)
                    .authorUsername("frneek")
                    .reactionLabel("LIKE")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment/reaction")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentReactionCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'commentId' && @.message == 'TASK_COMMENT_ID_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskCommentReactionIfTaskCommentDoesNotExist() throws Exception {
            // Given
            Long taskCommentId = 100L;
            CreateTaskCommentReactionCommand createTaskCommentReactionCommand = CreateTaskCommentReactionCommand.builder()
                    .commentId(taskCommentId)
                    .authorUsername("frneek")
                    .reactionLabel("LIKE")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment/reaction", taskCommentId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentReactionCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("TASK_COMMENT_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskCommentReactionWithNullAuthorUsername() throws Exception {
            // Given
            CreateTaskCommentReactionCommand createTaskCommentReactionCommand = CreateTaskCommentReactionCommand.builder()
                    .commentId(1L)
                    .authorUsername(null)
                    .reactionLabel("LIKE")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment/reaction")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentReactionCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'authorUsername' && @.message == 'AUTHOR_USERNAME_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskCommentReactionIfAuthorDoesNotExist() throws Exception {
            // Given
            String nonExistingUsername = "nonExistingUsername";
            CreateTaskCommentReactionCommand createTaskCommentReactionCommand = CreateTaskCommentReactionCommand.builder()
                    .commentId(1L)
                    .authorUsername(nonExistingUsername)
                    .reactionLabel("LIKE")
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
            mockMvc.perform(post("/task/comment/reaction")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentReactionCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskCommentWithNullReactionLabel() throws Exception {
            // Given
            CreateTaskCommentReactionCommand createTaskCommentReactionCommand = CreateTaskCommentReactionCommand.builder()
                    .commentId(1L)
                    .authorUsername("frneek")
                    .reactionLabel(null)
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment/reaction")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentReactionCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'reactionLabel' && @.message == 'REACTION_LABEL_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskCommentReactionWithInvalidReactionLabelValue() throws Exception {
            // Given
            CreateTaskCommentReactionCommand createTaskCommentReactionCommand = CreateTaskCommentReactionCommand.builder()
                    .commentId(1L)
                    .authorUsername("frneek")
                    .reactionLabel("INVALID")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment/reaction")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentReactionCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'reactionLabel' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.task_comment_reaction.ReactionLabel')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateTaskCommentReactionWithoutRoleHeader() throws Exception {
            // Given
            CreateTaskCommentReactionCommand createTaskCommentReactionCommand = CreateTaskCommentReactionCommand.builder()
                    .commentId(1L)
                    .authorUsername("frneek")
                    .reactionLabel("LIKE")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment/reaction")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createTaskCommentReactionCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"));
        }

        @Test
        void shouldNotCreateTaskCommentReactionWithoutUsernameHeader() throws Exception {
            // Given
            CreateTaskCommentReactionCommand createTaskCommentReactionCommand = CreateTaskCommentReactionCommand.builder()
                    .commentId(1L)
                    .authorUsername("frneek")
                    .reactionLabel("LIKE")
                    .build();
            // When
            // Then
            mockMvc.perform(post("/task/comment/reaction")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(createTaskCommentReactionCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"));
        }
    }
}