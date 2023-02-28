package com.kanwise.kanwise_service.controller.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanwise.kanwise_service.controller.DatabaseCleaner;
import com.kanwise.kanwise_service.model.project.command.CreateProjectCommand;
import com.kanwise.kanwise_service.model.project.command.EditProjectCommand;
import com.kanwise.kanwise_service.model.project.command.EditProjectPartiallyCommand;
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
import static java.util.Set.of;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
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
class ProjectControllerIT {

    private final MockMvc mockMvc;
    private final DatabaseCleaner databaseCleaner;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProjectControllerIT(MockMvc mockMvc, DatabaseCleaner databaseCleaner, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.databaseCleaner = databaseCleaner;
        this.objectMapper = objectMapper;
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }


    @Nested
    class ShouldCreateProject {

        @Test
        void shouldCreateProject() throws Exception {
            // Given
            CreateProjectCommand createProjectCommand = CreateProjectCommand.builder()
                    .title("kanwise-testing")
                    .description("kanwise-testing-description")
                    .membersUsernames(of("frneek"))
                    .build();
            // When
            // Then
            mockMvc.perform(post("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createProjectCommand)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(4))
                    .andExpect(jsonPath("$.title").value("kanwise-testing"))
                    .andExpect(jsonPath("$.description").value("kanwise-testing-description"))
                    .andExpect(jsonPath("$.membersCount").value(1))
                    .andExpect(jsonPath("$.tasksCount").value(0))
                    .andExpect(jsonPath("$.todoTaskCount").value(0))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(0))
                    .andExpect(jsonPath("$.doneTaskCount").value(0))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/4/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/4/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/4/statistics"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotCreateProject {

        @Test
        void shouldNotCreateProjectWithBlankTitle() throws Exception {
            // Given
            CreateProjectCommand createProjectCommand = CreateProjectCommand.builder()
                    .title("")
                    .description("kanwise-testing-description")
                    .membersUsernames(of("frneek"))
                    .build();
            // When
            // Then
            mockMvc.perform(post("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'title' && @.message == 'TITLE_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateProjectWithNullTitle() throws Exception {
            // Given
            CreateProjectCommand createProjectCommand = CreateProjectCommand.builder()
                    .title(null)
                    .description("kanwise-testing-description")
                    .membersUsernames(of("frneek"))
                    .build();
            // When
            // Then
            mockMvc.perform(post("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'title' && @.message == 'TITLE_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateProjectWithBlankDescription() throws Exception {
            // Given
            CreateProjectCommand createProjectCommand = CreateProjectCommand.builder()
                    .title("kanwise-testing")
                    .description("")
                    .membersUsernames(of("frneek"))
                    .build();
            // When
            // Then
            mockMvc.perform(post("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'description' && @.message == 'DESCRIPTION_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateProjectWithNullDescription() throws Exception {
            // Given
            CreateProjectCommand createProjectCommand = CreateProjectCommand.builder()
                    .title("kanwise-testing")
                    .description(null)
                    .membersUsernames(of("frneek"))
                    .build();
            // When
            // Then
            mockMvc.perform(post("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'description' && @.message == 'DESCRIPTION_NOT_BLANK')]").exists())
                    .andDo(print());
        }


        @Test
        void shouldNotCreateProjectWithNullMembersUsernames() throws Exception {
            // Given
            CreateProjectCommand createProjectCommand = CreateProjectCommand.builder()
                    .title("kanwise-testing")
                    .description("kanwise-testing-description")
                    .membersUsernames(null)
                    .build();
            // When
            // Then
            mockMvc.perform(post("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'membersUsernames' && @.message == 'MEMBERS_USERNAMES_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotCreateProjectWithoutRoleHeader() throws Exception {
            // Given
            CreateProjectCommand createProjectCommand = CreateProjectCommand.builder()
                    .title("kanwise-testing")
                    .description("kanwise-testing-description")
                    .membersUsernames(of("frneek"))
                    .build();
            // When
            // Then
            mockMvc.perform(post("/project")
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(createProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotCreateProjectWithoutUsernameHeader() throws Exception {
            // Given
            CreateProjectCommand createProjectCommand = CreateProjectCommand.builder()
                    .title("kanwise-testing")
                    .description("kanwise-testing-description")
                    .membersUsernames(of("frneek"))
                    .build();
            // When
            // Then
            mockMvc.perform(post("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(createProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldFindProject {

        @Test
        void shouldFindProject() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotFindProject {

        @Test
        void shouldNotFindProjectIfProjectDoesNotExist() throws Exception {
            // Given
            Long nonExistingProjectId = 999L;
            // When
            // Then
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
        }

        @Test
        void shouldNotFindProjectWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}", projectId)
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
        void shouldNotFindProjectWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}", projectId)
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
    class ShouldFindProjects {

        @Test
        void shouldFindProjectsWithDefaultPagination() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.content[0].description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.content[0].createdAt").exists())
                    .andExpect(jsonPath("$.content[0].membersCount").value(2))
                    .andExpect(jsonPath("$.content[0].tasksCount").value(6))
                    .andExpect(jsonPath("$.content[0].todoTaskCount").value(3))
                    .andExpect(jsonPath("$.content[0].inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.content[0].doneTaskCount").value(2))
                    .andExpect(jsonPath("$.content[0].joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.content[0].status").value("CREATED"))
                    .andExpect(jsonPath("$.content[0].links").isArray())
                    .andExpect(jsonPath("$.content[0].links[0].rel").value("project-members"))
                    .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$.content[0].links[1].rel").value("project-tasks"))
                    .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$.content[0].links[2].rel").value("project-statistics"))
                    .andExpect(jsonPath("$.content[0].links[2].href").value("http://localhost/project/1/statistics"))
                    .andExpect(jsonPath("$.content[1].id").value(2))
                    .andExpect(jsonPath("$.content[1].title").value("Kanwise-Frontend"))
                    .andExpect(jsonPath("$.content[1].description").value("This is the frontend component of the Kanwise application. It is responsible for rendering the user interface and handling user interactions. It communicates with the backend to retrieve data and perform actions."))
                    .andExpect(jsonPath("$.content[1].createdAt").exists())
                    .andExpect(jsonPath("$.content[1].membersCount").value(1))
                    .andExpect(jsonPath("$.content[1].tasksCount").value(4))
                    .andExpect(jsonPath("$.content[1].todoTaskCount").value(2))
                    .andExpect(jsonPath("$.content[1].inProgressTaskCount").value(2))
                    .andExpect(jsonPath("$.content[1].doneTaskCount").value(0))
                    .andExpect(jsonPath("$.content[1].joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.content[1].status").value("ON_HOLD"))
                    .andExpect(jsonPath("$.content[1].links").isArray())
                    .andExpect(jsonPath("$.content[1].links[0].rel").value("project-members"))
                    .andExpect(jsonPath("$.content[1].links[0].href").value("http://localhost/project/2/members"))
                    .andExpect(jsonPath("$.content[1].links[1].rel").value("project-tasks"))
                    .andExpect(jsonPath("$.content[1].links[1].href").value("http://localhost/project/2/tasks"))
                    .andExpect(jsonPath("$.content[1].links[2].rel").value("project-statistics"))
                    .andExpect(jsonPath("$.content[1].links[2].href").value("http://localhost/project/2/statistics"))
                    .andExpect(jsonPath("$.content[2].id").value(3))
                    .andExpect(jsonPath("$.content[2].title").value("Kanwise-DevOps"))
                    .andExpect(jsonPath("$.content[2].description").value("This project manages the deployment, monitoring, and maintenance of the Kanwise application. It includes tasks such as setting up infrastructure, automating builds and deployments, and monitoring the app's performance and stability."))
                    .andExpect(jsonPath("$.content[2].createdAt").exists())
                    .andExpect(jsonPath("$.content[2].membersCount").value(1))
                    .andExpect(jsonPath("$.content[2].tasksCount").value(5))
                    .andExpect(jsonPath("$.content[2].todoTaskCount").value(3))
                    .andExpect(jsonPath("$.content[2].inProgressTaskCount").value(2))
                    .andExpect(jsonPath("$.content[2].doneTaskCount").value(0))
                    .andExpect(jsonPath("$.content[2].joinRequestsCount").value(1))
                    .andExpect(jsonPath("$.content[2].status").value("ON_TRACK"))
                    .andExpect(jsonPath("$.content[2].links").isArray())
                    .andExpect(jsonPath("$.content[2].links[0].rel").value("project-members"))
                    .andExpect(jsonPath("$.content[2].links[0].href").value("http://localhost/project/3/members"))
                    .andExpect(jsonPath("$.content[2].links[1].rel").value("project-tasks"))
                    .andExpect(jsonPath("$.content[2].links[1].href").value("http://localhost/project/3/tasks"))
                    .andExpect(jsonPath("$.content[2].links[2].rel").value("project-statistics"))
                    .andExpect(jsonPath("$.content[2].links[2].href").value("http://localhost/project/3/statistics"))
                    .andExpect(jsonPath("$.last").value(true))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.first").value(true))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.sort").exists())
                    .andExpect(jsonPath("$.sort.empty").value(false))
                    .andExpect(jsonPath("$.sort.unsorted").value(false))
                    .andExpect(jsonPath("$.sort.sorted").value(true))
                    .andExpect(jsonPath("$.numberOfElements").value(3))
                    .andExpect(jsonPath("$.empty").value(false))
                    .andDo(print());
        }

        @Test
        void shouldFindProjectsWithTitle() throws Exception {
            // Given
            String title = "DevOps";
            // When
            // Then
            mockMvc.perform(get("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("title", title))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(3))
                    .andExpect(jsonPath("$.content[0].title").value("Kanwise-DevOps"))
                    .andExpect(jsonPath("$.content[0].description").value("This project manages the deployment, monitoring, and maintenance of the Kanwise application. It includes tasks such as setting up infrastructure, automating builds and deployments, and monitoring the app's performance and stability."))
                    .andExpect(jsonPath("$.content[0].createdAt").exists())
                    .andExpect(jsonPath("$.content[0].membersCount").value(1))
                    .andExpect(jsonPath("$.content[0].tasksCount").value(5))
                    .andExpect(jsonPath("$.content[0].todoTaskCount").value(3))
                    .andExpect(jsonPath("$.content[0].inProgressTaskCount").value(2))
                    .andExpect(jsonPath("$.content[0].doneTaskCount").value(0))
                    .andExpect(jsonPath("$.content[0].joinRequestsCount").value(1))
                    .andExpect(jsonPath("$.content[0].status").value("ON_TRACK"))
                    .andExpect(jsonPath("$.content[0].links").isArray())
                    .andExpect(jsonPath("$.content[0].links[0].rel").value("project-members"))
                    .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/project/3/members"))
                    .andExpect(jsonPath("$.content[0].links[1].rel").value("project-tasks"))
                    .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/project/3/tasks"))
                    .andExpect(jsonPath("$.content[0].links[2].rel").value("project-statistics"))
                    .andExpect(jsonPath("$.content[0].links[2].href").value("http://localhost/project/3/statistics"))
                    .andExpect(jsonPath("$.last").value(true))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.first").value(true))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.sort").exists())
                    .andExpect(jsonPath("$.sort.empty").value(false))
                    .andExpect(jsonPath("$.sort.unsorted").value(false))
                    .andExpect(jsonPath("$.sort.sorted").value(true))
                    .andExpect(jsonPath("$.numberOfElements").value(1))
                    .andExpect(jsonPath("$.empty").value(false))
                    .andDo(print());
        }

        @Test
        void shouldFindProjectsWithCustomSortDirection() throws Exception {
            // Given
            String sortDirection = "desc";
            // When
            // Then
            mockMvc.perform(get("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("sortDirection", sortDirection))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(3))
                    .andExpect(jsonPath("$.content[0].title").value("Kanwise-DevOps"))
                    .andExpect(jsonPath("$.content[0].description").value("This project manages the deployment, monitoring, and maintenance of the Kanwise application. It includes tasks such as setting up infrastructure, automating builds and deployments, and monitoring the app's performance and stability."))
                    .andExpect(jsonPath("$.content[0].createdAt").exists())
                    .andExpect(jsonPath("$.content[0].membersCount").value(1))
                    .andExpect(jsonPath("$.content[0].tasksCount").value(5))
                    .andExpect(jsonPath("$.content[0].todoTaskCount").value(3))
                    .andExpect(jsonPath("$.content[0].inProgressTaskCount").value(2))
                    .andExpect(jsonPath("$.content[0].doneTaskCount").value(0))
                    .andExpect(jsonPath("$.content[0].joinRequestsCount").value(1))
                    .andExpect(jsonPath("$.content[0].status").value("ON_TRACK"))
                    .andExpect(jsonPath("$.content[0].links").isArray())
                    .andExpect(jsonPath("$.content[0].links[0].rel").value("project-members"))
                    .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/project/3/members"))
                    .andExpect(jsonPath("$.content[0].links[1].rel").value("project-tasks"))
                    .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/project/3/tasks"))
                    .andExpect(jsonPath("$.content[0].links[2].rel").value("project-statistics"))
                    .andExpect(jsonPath("$.content[0].links[2].href").value("http://localhost/project/3/statistics"))
                    .andExpect(jsonPath("$.content[1].id").value(2))
                    .andExpect(jsonPath("$.content[1].title").value("Kanwise-Frontend"))
                    .andExpect(jsonPath("$.content[1].description").value("This is the frontend component of the Kanwise application. It is responsible for rendering the user interface and handling user interactions. It communicates with the backend to retrieve data and perform actions."))
                    .andExpect(jsonPath("$.content[1].createdAt").exists())
                    .andExpect(jsonPath("$.content[1].membersCount").value(1))
                    .andExpect(jsonPath("$.content[1].tasksCount").value(4))
                    .andExpect(jsonPath("$.content[1].todoTaskCount").value(2))
                    .andExpect(jsonPath("$.content[1].inProgressTaskCount").value(2))
                    .andExpect(jsonPath("$.content[1].doneTaskCount").value(0))
                    .andExpect(jsonPath("$.content[1].joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.content[1].status").value("ON_HOLD"))
                    .andExpect(jsonPath("$.content[1].links").isArray())
                    .andExpect(jsonPath("$.content[1].links[0].rel").value("project-members"))
                    .andExpect(jsonPath("$.content[1].links[0].href").value("http://localhost/project/2/members"))
                    .andExpect(jsonPath("$.content[1].links[1].rel").value("project-tasks"))
                    .andExpect(jsonPath("$.content[1].links[1].href").value("http://localhost/project/2/tasks"))
                    .andExpect(jsonPath("$.content[1].links[2].rel").value("project-statistics"))
                    .andExpect(jsonPath("$.content[1].links[2].href").value("http://localhost/project/2/statistics"))
                    .andExpect(jsonPath("$.content[2].id").value(1))
                    .andExpect(jsonPath("$.content[2].title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.content[2].description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.content[2].createdAt").exists())
                    .andExpect(jsonPath("$.content[2].membersCount").value(2))
                    .andExpect(jsonPath("$.content[2].tasksCount").value(6))
                    .andExpect(jsonPath("$.content[2].todoTaskCount").value(3))
                    .andExpect(jsonPath("$.content[2].inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.content[2].doneTaskCount").value(2))
                    .andExpect(jsonPath("$.content[2].joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.content[2].status").value("CREATED"))
                    .andExpect(jsonPath("$.content[2].links").isArray())
                    .andExpect(jsonPath("$.content[2].links[0].rel").value("project-members"))
                    .andExpect(jsonPath("$.content[2].links[0].href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$.content[2].links[1].rel").value("project-tasks"))
                    .andExpect(jsonPath("$.content[2].links[1].href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$.content[2].links[2].rel").value("project-statistics"))
                    .andExpect(jsonPath("$.content[2].links[2].href").value("http://localhost/project/1/statistics"))
                    .andExpect(jsonPath("$.last").value(true))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.first").value(true))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.sort").exists())
                    .andExpect(jsonPath("$.sort.empty").value(false))
                    .andExpect(jsonPath("$.sort.unsorted").value(false))
                    .andExpect(jsonPath("$.sort.sorted").value(true))
                    .andExpect(jsonPath("$.numberOfElements").value(3))
                    .andExpect(jsonPath("$.empty").value(false))
                    .andDo(print());
        }

        @Test
        void shouldFindProjectsWithCustomPageNumberAndPageSize() throws Exception {
            // Given
            int pageNumber = 1;
            int pageSize = 2;
            // When
            // Then
            mockMvc.perform(get("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("pageSize", String.valueOf(pageSize))
                            .param("pageNumber", String.valueOf(pageNumber)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(3))
                    .andExpect(jsonPath("$.content[0].title").value("Kanwise-DevOps"))
                    .andExpect(jsonPath("$.content[0].description").value("This project manages the deployment, monitoring, and maintenance of the Kanwise application. It includes tasks such as setting up infrastructure, automating builds and deployments, and monitoring the app's performance and stability."))
                    .andExpect(jsonPath("$.content[0].createdAt").exists())
                    .andExpect(jsonPath("$.content[0].membersCount").value(1))
                    .andExpect(jsonPath("$.content[0].tasksCount").value(5))
                    .andExpect(jsonPath("$.content[0].todoTaskCount").value(3))
                    .andExpect(jsonPath("$.content[0].inProgressTaskCount").value(2))
                    .andExpect(jsonPath("$.content[0].doneTaskCount").value(0))
                    .andExpect(jsonPath("$.content[0].joinRequestsCount").value(1))
                    .andExpect(jsonPath("$.content[0].status").value("ON_TRACK"))
                    .andExpect(jsonPath("$.content[0].links").isArray())
                    .andExpect(jsonPath("$.content[0].links[0].rel").value("project-members"))
                    .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/project/3/members"))
                    .andExpect(jsonPath("$.content[0].links[1].rel").value("project-tasks"))
                    .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/project/3/tasks"))
                    .andExpect(jsonPath("$.content[0].links[2].rel").value("project-statistics"))
                    .andExpect(jsonPath("$.content[0].links[2].href").value("http://localhost/project/3/statistics"))
                    .andExpect(jsonPath("$.last").value(true))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.first").value(false))
                    .andExpect(jsonPath("$.size").value(pageSize))
                    .andExpect(jsonPath("$.number").value(pageNumber))
                    .andExpect(jsonPath("$.sort").exists())
                    .andExpect(jsonPath("$.sort.empty").value(false))
                    .andExpect(jsonPath("$.sort.unsorted").value(false))
                    .andExpect(jsonPath("$.sort.sorted").value(true))
                    .andExpect(jsonPath("$.numberOfElements").value(1))
                    .andExpect(jsonPath("$.empty").value(false))
                    .andDo(print());
        }

        @Test
        void shouldFindProjectsWithCustomSortBy() throws Exception {
            // Given
            String sortBy = "title";
            // When
            // Then
            mockMvc.perform(get("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("sortBy", sortBy))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.content[0].description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.content[0].createdAt").exists())
                    .andExpect(jsonPath("$.content[0].membersCount").value(2))
                    .andExpect(jsonPath("$.content[0].tasksCount").value(6))
                    .andExpect(jsonPath("$.content[0].todoTaskCount").value(3))
                    .andExpect(jsonPath("$.content[0].inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.content[0].doneTaskCount").value(2))
                    .andExpect(jsonPath("$.content[0].joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.content[0].status").value("CREATED"))
                    .andExpect(jsonPath("$.content[0].links").isArray())
                    .andExpect(jsonPath("$.content[0].links[0].rel").value("project-members"))
                    .andExpect(jsonPath("$.content[0].links[0].href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$.content[0].links[1].rel").value("project-tasks"))
                    .andExpect(jsonPath("$.content[0].links[1].href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$.content[0].links[2].rel").value("project-statistics"))
                    .andExpect(jsonPath("$.content[0].links[2].href").value("http://localhost/project/1/statistics"))
                    .andExpect(jsonPath("$.content[1].id").value(3))
                    .andExpect(jsonPath("$.content[1].title").value("Kanwise-DevOps"))
                    .andExpect(jsonPath("$.content[1].description").value("This project manages the deployment, monitoring, and maintenance of the Kanwise application. It includes tasks such as setting up infrastructure, automating builds and deployments, and monitoring the app's performance and stability."))
                    .andExpect(jsonPath("$.content[1].createdAt").exists())
                    .andExpect(jsonPath("$.content[1].membersCount").value(1))
                    .andExpect(jsonPath("$.content[1].tasksCount").value(5))
                    .andExpect(jsonPath("$.content[1].todoTaskCount").value(3))
                    .andExpect(jsonPath("$.content[1].inProgressTaskCount").value(2))
                    .andExpect(jsonPath("$.content[1].doneTaskCount").value(0))
                    .andExpect(jsonPath("$.content[1].joinRequestsCount").value(1))
                    .andExpect(jsonPath("$.content[1].status").value("ON_TRACK"))
                    .andExpect(jsonPath("$.content[1].links").isArray())
                    .andExpect(jsonPath("$.content[1].links[0].rel").value("project-members"))
                    .andExpect(jsonPath("$.content[1].links[0].href").value("http://localhost/project/3/members"))
                    .andExpect(jsonPath("$.content[1].links[1].rel").value("project-tasks"))
                    .andExpect(jsonPath("$.content[1].links[1].href").value("http://localhost/project/3/tasks"))
                    .andExpect(jsonPath("$.content[1].links[2].rel").value("project-statistics"))
                    .andExpect(jsonPath("$.content[1].links[2].href").value("http://localhost/project/3/statistics"))
                    .andExpect(jsonPath("$.content[2].id").value(2))
                    .andExpect(jsonPath("$.content[2].title").value("Kanwise-Frontend"))
                    .andExpect(jsonPath("$.content[2].description").value("This is the frontend component of the Kanwise application. It is responsible for rendering the user interface and handling user interactions. It communicates with the backend to retrieve data and perform actions."))
                    .andExpect(jsonPath("$.content[2].createdAt").exists())
                    .andExpect(jsonPath("$.content[2].membersCount").value(1))
                    .andExpect(jsonPath("$.content[2].tasksCount").value(4))
                    .andExpect(jsonPath("$.content[2].todoTaskCount").value(2))
                    .andExpect(jsonPath("$.content[2].inProgressTaskCount").value(2))
                    .andExpect(jsonPath("$.content[2].doneTaskCount").value(0))
                    .andExpect(jsonPath("$.content[2].joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.content[2].status").value("ON_HOLD"))
                    .andExpect(jsonPath("$.content[2].links").isArray())
                    .andExpect(jsonPath("$.content[2].links[0].rel").value("project-members"))
                    .andExpect(jsonPath("$.content[2].links[0].href").value("http://localhost/project/2/members"))
                    .andExpect(jsonPath("$.content[2].links[1].rel").value("project-tasks"))
                    .andExpect(jsonPath("$.content[2].links[1].href").value("http://localhost/project/2/tasks"))
                    .andExpect(jsonPath("$.content[2].links[2].rel").value("project-statistics"))
                    .andExpect(jsonPath("$.content[2].links[2].href").value("http://localhost/project/2/statistics"))
                    .andExpect(jsonPath("$.last").value(true))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.first").value(true))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.sort").exists())
                    .andExpect(jsonPath("$.sort.empty").value(false))
                    .andExpect(jsonPath("$.sort.unsorted").value(false))
                    .andExpect(jsonPath("$.sort.sorted").value(true))
                    .andExpect(jsonPath("$.numberOfElements").value(3))
                    .andExpect(jsonPath("$.empty").value(false))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotFindProjects {

        @Test
        void shouldNotFindProjectsWithNegativePageNumber() throws Exception {
            // Given
            int pageNumber = -1;
            // When
            // Then
            mockMvc.perform(get("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("pageNumber", String.valueOf(pageNumber)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'pageNumber' && @.message == 'PAGE_NOT_NEGATIVE')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotFindProjectsWithInvalidPageSize() throws Exception {
            // Given
            int pageSize = 0;
            // When
            // Then
            mockMvc.perform(get("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("pageSize", String.valueOf(pageSize)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'pageSize' && @.message == 'PAGE_SIZE_NOT_LESS_THAN_ONE')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotFindProjectsWithInvalidSortBy() throws Exception {
            // Given
            String sortBy = "invalid";
            // When
            // Then
            mockMvc.perform(get("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("sortBy", sortBy))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'sortBy' && @.message == 'INVALID_SORT_BY_VALUE_FIELD')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotFindProjectsWithInvalidSortDirection() throws Exception {
            // Given
            String sortDirection = "invalid";
            // When
            // Then
            mockMvc.perform(get("/project")
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("sortDirection", sortDirection))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'sortDirection' && @.message == 'INVALID_SORT_DIRECTION')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotFindProjectsWithoutRoleHeader() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/project")
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
        void shouldNotFindProjectsWithoutUsernameHeader() throws Exception {
            // Given
            // When
            // Then
            mockMvc.perform(get("/project")
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
    class ShouldEditProject {

        @Test
        void shouldEditProject() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .title("Edited title")
                    .description("Edited description")
                    .status("ON_TRACK")
                    .build();
            // When
            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());
            // Then
            mockMvc.perform(put("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value(editProjectCommand.title()))
                    .andExpect(jsonPath("$.description").value(editProjectCommand.description()))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value(editProjectCommand.status()))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());

            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value(editProjectCommand.title()))
                    .andExpect(jsonPath("$.description").value(editProjectCommand.description()))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value(editProjectCommand.status()))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotEditProject {

        @Test
        void shouldNotEditProjectIfProjectDoesNotExist() throws Exception {
            // Given
            Long projectId = 100L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .title("Edited title")
                    .description("Edited description")
                    .status("ON_TRACK")
                    .build();
            // When
            // Then
            mockMvc.perform(put("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
        }


        @Test
        void shouldNotEditProjectIfTitleIsNull() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .title(null)
                    .description("Edited description")
                    .status("ON_TRACK")
                    .build();
            // When
            // Then
            mockMvc.perform(put("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'title' && @.message == 'TITLE_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditProjectIfTitleIsBlank() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .title(" ")
                    .description("Edited description")
                    .status("ON_TRACK")
                    .build();
            // When
            // Then
            mockMvc.perform(put("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'title' && @.message == 'TITLE_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditProjectIfDescriptionIsNull() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .title("Edited title")
                    .description(null)
                    .status("ON_TRACK")
                    .build();
            // When
            // Then
            mockMvc.perform(put("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'description' && @.message == 'DESCRIPTION_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditProjectIfDescriptionIsBlank() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .title("Edited title")
                    .description(" ")
                    .status("ON_TRACK")
                    .build();
            // When
            // Then
            mockMvc.perform(put("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'description' && @.message == 'DESCRIPTION_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditProjectIfStatusIsNull() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .title("Edited title")
                    .description("Edited description")
                    .status(null)
                    .build();
            // When
            // Then
            mockMvc.perform(put("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'status' && @.message == 'STATUS_NOT_NULL')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditProjectWithInvalidStatusValue() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .title("Edited title")
                    .description("Edited description")
                    .status("INVALID_STATUS")
                    .build();
            // When
            // Then
            mockMvc.perform(put("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'status' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.project.ProjectStatus')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditProjectWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .title("Edited title")
                    .description("Edited description")
                    .status("ON_TRACK")
                    .build();
            // When
            // Then
            mockMvc.perform(put("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotEditProjectWithUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .title("Edited title")
                    .description("Edited description")
                    .status("ON_TRACK")
                    .build();
            // When
            // Then
            mockMvc.perform(put("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldEditProjectPartially {

        @Test
        void shouldEditProjectPartiallyWithTitleOnly() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .title("Edited title")
                    .build();
            // When
            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value(editProjectCommand.title()))
                    .andExpect(jsonPath("$.description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());

            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value(editProjectCommand.title()))
                    .andExpect(jsonPath("$.description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());
        }

        @Test
        void shouldEditProjectPartiallyWithDescriptionOnly() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .description("Edited description")
                    .build();
            // When
            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.description").value(editProjectCommand.description()))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());

            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.description").value(editProjectCommand.description()))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());
        }

        @Test
        void shouldEditProjectPartiallyStatusOnly() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectCommand editProjectCommand = EditProjectCommand.
                    builder()
                    .status("COMPLETED")
                    .build();
            // When
            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());
            // Then
            mockMvc.perform(patch("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value(editProjectCommand.status()))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());

            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value(editProjectCommand.status()))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotEditProjectPartially {

        @Test
        void shouldNotEditProjectPartiallyIfTitleIsBlank() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectPartiallyCommand editProjectCommand = EditProjectPartiallyCommand.
                    builder()
                    .title(" ")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'title' && @.message == 'TITLE_NULL_OR_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditProjectPartiallyIfDescriptionIsBlank() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectPartiallyCommand editProjectCommand = EditProjectPartiallyCommand.
                    builder()
                    .description(" ")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'description' && @.message == 'DESCRIPTION_NULL_OR_NOT_BLANK')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditProjectPartiallyWithInvalidStatusValue() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectPartiallyCommand editProjectCommand = EditProjectPartiallyCommand.
                    builder()
                    .status("INVALID")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.[?(@.field == 'status' && @.message == 'MUST_BE_ANY_OF_class com.kanwise.kanwise_service.model.project.ProjectStatus')]").exists())
                    .andDo(print());
        }

        @Test
        void shouldNotEditProjectPartiallyWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectPartiallyCommand editProjectCommand = EditProjectPartiallyCommand.
                    builder()
                    .title("Kanwise-Backend")
                    .description("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems.")
                    .status("COMPLETED")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotEditProjectPartiallyWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            EditProjectPartiallyCommand editProjectCommand = EditProjectPartiallyCommand.
                    builder()
                    .title("Kanwise-Backend")
                    .description("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems.")
                    .status("COMPLETED")
                    .build();
            // When
            // Then
            mockMvc.perform(patch("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .content(objectMapper.writeValueAsString(editProjectCommand)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldDeleteProject {

        @Test
        void shouldDeleteProject() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(projectId))
                    .andExpect(jsonPath("$.title").value("Kanwise-Backend"))
                    .andExpect(jsonPath("$.description").value("This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems."))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.membersCount").value(2))
                    .andExpect(jsonPath("$.tasksCount").value(6))
                    .andExpect(jsonPath("$.todoTaskCount").value(3))
                    .andExpect(jsonPath("$.inProgressTaskCount").value(1))
                    .andExpect(jsonPath("$.doneTaskCount").value(2))
                    .andExpect(jsonPath("$.joinRequestsCount").value(0))
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$._links.project-members.href").value("http://localhost/project/1/members"))
                    .andExpect(jsonPath("$._links.project-tasks.href").value("http://localhost/project/1/tasks"))
                    .andExpect(jsonPath("$._links.project-statistics.href").value("http://localhost/project/1/statistics"))
                    .andDo(print());
            // Then
            mockMvc.perform(delete("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotDeleteProject {

        @Test
        void shouldNotDeleteProjectIfProjectDoesNotExist() throws Exception {
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
            mockMvc.perform(delete("/project/{id}", projectId)
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
        void shouldNotDeleteProjectWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(delete("/project/{id}", projectId)
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
        void shouldNotDeleteProjectWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(delete("/project/{id}", projectId)
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
    class ShouldFindMembersForProject {

        @Test
        void shouldFindMembersForProject() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].username", containsInAnyOrder("frneek", "jaroslawPsikuta")))
                    .andExpect(jsonPath("$[*].projectCount", containsInAnyOrder(3, 1)))
                    .andExpect(jsonPath("$[*].commentsCount", containsInAnyOrder(3, 2)))
                    .andExpect(jsonPath("$[*].tasksCount", containsInAnyOrder(15, 3)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_DELETED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_REMOVED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_UPDATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_ASSIGNED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_CREATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_ASSIGNED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_DELETED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_CREATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_ADDED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("projects", "tasks", "task-comments", "join-requests", "join-responses", "statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/member/frneek/projects",
                            "http://localhost/member/frneek/tasks",
                            "http://localhost/member/frneek/comments",
                            "http://localhost/member/frneek/join/requests",
                            "http://localhost/member/frneek/join/responses",
                            "http://localhost/member/frneek/statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/member/jaroslawPsikuta/projects",
                            "http://localhost/member/jaroslawPsikuta/tasks",
                            "http://localhost/member/jaroslawPsikuta/comments",
                            "http://localhost/member/jaroslawPsikuta/join/requests",
                            "http://localhost/member/jaroslawPsikuta/join/responses",
                            "http://localhost/member/jaroslawPsikuta/statistics")))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotFindMembersForProject {

        @Test
        void shouldNotFindMembersForProjectIfProjectDoesNotExist() throws Exception {
            // Given
            Long projectId = 100L;
            // When
            mockMvc.perform(get("/project/{id}/members", projectId)
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
            mockMvc.perform(get("/project/{id}/members", projectId)
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
        void shouldNotFindMembersForProjectWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/members", projectId)
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
        void shouldNotFindMembersForProjectWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/members", projectId)
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
    class ShouldFindTasksForProject {

        @Test
        void shouldFindTasksForProject() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}/tasks", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(6)))
                    .andExpect(jsonPath("$[*].taskId", containsInAnyOrder(1, 2, 3, 4, 5, 8)))
                    .andExpect(jsonPath("$[*].projectId", containsInAnyOrder(1, 1, 1, 1, 1, 1)))
                    .andExpect(jsonPath("$[*].authorUsername", containsInAnyOrder("frneek", "frneek", "frneek", "frneek", "frneek", "frneek")))
                    .andExpect(jsonPath("$[*].assignedMembersCount", containsInAnyOrder(2, 2, 2, 1, 1, 1)))
                    .andExpect(jsonPath("$[*].commentsCount", containsInAnyOrder(5, 0, 0, 0, 0, 0)))
                    .andExpect(jsonPath("$[*].statusesCount", containsInAnyOrder(1, 1, 1, 2, 3, 3)))
                    .andExpect(jsonPath("$[*].title").exists())
                    .andExpect(jsonPath("$[*].description").exists())
                    .andExpect(jsonPath("$[*].priority", containsInAnyOrder("NORMAL", "NORMAL", "URGENT", "HIGH", "HIGH", "LOW")))
                    .andExpect(jsonPath("$[*].type", containsInAnyOrder("NEW_FEATURE", "NEW_FEATURE", "NEW_FEATURE", "IMPROVEMENT", "BUG", "TEST")))
                    .andExpect(jsonPath("$[*].estimatedTime", containsInAnyOrder(64800.000000000, 86400.000000000, 21600.000000000, 14400.000000000, 21600.000000000, 10800.000000000)))
                    .andExpect(jsonPath("$[*].currentStatus", containsInAnyOrder("IN_PROGRESS", "TODO", "TODO", "TODO", "RESOLVED", "RESOLVED")))
                    .andExpect(jsonPath("$[*].links", hasSize(6)))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("project", "author", "assigned-members", "statistics", "comments", "statuses")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/project/1",
                            "http://localhost/member/frneek",
                            "http://localhost/task/1/members",
                            "http://localhost/task/1/statistics",
                            "http://localhost/task/1/comments",
                            "http://localhost/task/1/statuses"
                    )))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/project/1",
                            "http://localhost/member/frneek",
                            "http://localhost/task/2/members",
                            "http://localhost/task/2/statistics",
                            "http://localhost/task/2/comments",
                            "http://localhost/task/2/statuses"
                    )))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/project/1",
                            "http://localhost/member/frneek",
                            "http://localhost/task/3/members",
                            "http://localhost/task/3/statistics",
                            "http://localhost/task/3/comments",
                            "http://localhost/task/3/statuses"
                    )))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/project/1",
                            "http://localhost/member/frneek",
                            "http://localhost/task/4/members",
                            "http://localhost/task/4/statistics",
                            "http://localhost/task/4/comments",
                            "http://localhost/task/4/statuses"
                    )))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/project/1",
                            "http://localhost/member/frneek",
                            "http://localhost/task/5/members",
                            "http://localhost/task/5/statistics",
                            "http://localhost/task/5/comments",
                            "http://localhost/task/5/statuses"
                    )))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/project/1",
                            "http://localhost/member/frneek",
                            "http://localhost/task/8/members",
                            "http://localhost/task/8/statistics",
                            "http://localhost/task/8/comments",
                            "http://localhost/task/8/statuses"
                    )))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotFindTasksForProject {

        @Test
        void shouldNotFindTasksForProjectIfProjectDoesNotExist() throws Exception {
            // Given
            Long nonExistingProjectId = 100L;
            // When
            mockMvc.perform(get("/project/{id}", nonExistingProjectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
            // Then
            mockMvc.perform(get("/project/{id}", nonExistingProjectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotFindTasksForProjectWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotFindTasksForProjectWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            // When
            // Then
            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }

    }

    @Nested
    class ShouldFindMemberTasksForProject {

        @Test
        void shouldFindMemberTasksForProject() throws Exception {
            // Given
            String memberUsername = "jaroslawPsikuta";
            Long projectId = 1L;
            // When
            mockMvc.perform(get("/project/{id}/tasks", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("username", memberUsername))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[*].taskId", containsInAnyOrder(1, 4, 5)))
                    .andExpect(jsonPath("$[*].projectId", containsInAnyOrder(1, 1, 1)))
                    .andExpect(jsonPath("$[*].authorUsername", containsInAnyOrder("frneek", "frneek", "frneek")))
                    .andExpect(jsonPath("$[*].assignedMembersCount", containsInAnyOrder(2, 2, 2)))
                    .andExpect(jsonPath("$[*].commentsCount", containsInAnyOrder(0, 0, 0)))
                    .andExpect(jsonPath("$[*].statusesCount", containsInAnyOrder(1, 1, 1)))
                    .andExpect(jsonPath("$[*].title", containsInAnyOrder("Implement APIs for creating and updating kanban boards", "Add integration with a notification service ", "Add unit tests for the kanban board APIs ")))
                    .andExpect(jsonPath("$[*].description", containsInAnyOrder("This task involves developing the APIs that allow users to create and update kanban boards in the application.", "This task involves integrating the backend with a notification service to allow users to receive notifications in the application.", "This task involves writing unit tests to ensure that the APIs for creating and updating kanban boards are working correctly.")))
                    .andExpect(jsonPath("$[*].priority", containsInAnyOrder("NORMAL", "LOW", "HIGH")))
                    .andExpect(jsonPath("$[*].type", containsInAnyOrder("NEW_FEATURE", "NEW_FEATURE", "TEST")))
                    .andExpect(jsonPath("$[*].estimatedTime", containsInAnyOrder(86400.000000000, 21600.000000000, 21600.000000000)))
                    .andExpect(jsonPath("$[*].currentStatus", containsInAnyOrder("TODO", "TODO", "TODO")))
                    .andExpect(jsonPath("$[*].links[*].rel", containsInAnyOrder("project", "author", "assigned-members", "statistics", "comments", "statuses", "project", "author", "assigned-members", "statistics", "comments", "statuses", "project", "author", "assigned-members", "statistics", "comments", "statuses")))
                    .andExpect(jsonPath("$[*].links[*].href", containsInAnyOrder("http://localhost/project/1", "http://localhost/member/frneek", "http://localhost/task/1/members", "http://localhost/task/1/statistics", "http://localhost/task/1/comments", "http://localhost/task/1/statuses", "http://localhost/project/1", "http://localhost/member/frneek", "http://localhost/task/4/members", "http://localhost/task/4/statistics", "http://localhost/task/4/comments", "http://localhost/task/4/statuses", "http://localhost/project/1", "http://localhost/member/frneek", "http://localhost/task/5/members", "http://localhost/task/5/statistics", "http://localhost/task/5/comments", "http://localhost/task/5/statuses")))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotFindMemberTasksForProject {

        @Test
        void shouldNotFindMemberTasksForProjectIfProjectDoesNotExist() throws Exception {
            // Given
            Long nonExistingProjectId = 100L;
            String memberUsername = "jaroslawPsikuta";
            // When
            mockMvc.perform(get("/project/{id}", nonExistingProjectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("username", memberUsername))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
            // Then
            mockMvc.perform(get("/project/{id}/tasks", nonExistingProjectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("username", memberUsername))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotFindMemberTasksForProjectIfMemberIsNotAssignedToProject() throws Exception {
            // Given
            Long projectId = 2L;
            String memberUsername = "jaroslawPsikuta";
            // When
            // Then
            mockMvc.perform(get("/project/{id}/tasks", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("username", memberUsername))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("MEMBER_WITH_USERNAME_jaroslawPsikuta_IS_NOT_ASSIGNED_TO_PROJECT_WITH_ID_2"))
                    .andDo(print());
        }

        @Test
        void shouldNotFindMemberTasksForProjectWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            String memberUsername = "jaroslawPsikuta";
            // When
            // Then
            mockMvc.perform(get("/project/{id}/tasks", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .param("username", memberUsername))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotFindMemberTasksForProjectWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            String memberUsername = "jaroslawPsikuta";
            // When
            // Then
            mockMvc.perform(get("/project/{id}/tasks", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .param("username", memberUsername))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldAddMemberToProject {

        @Test
        void shouldAddMemberToProject() throws Exception {
            // Given
            Long projectId = 2L;
            String memberToAddUsername = "jaroslawPsikuta";
            // When
            // Then
            mockMvc.perform(post("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("usernames", memberToAddUsername))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].username").value(memberToAddUsername))
                    .andExpect(jsonPath("$[0].projectCount").value(2))
                    .andExpect(jsonPath("$[0].commentsCount").value(2))
                    .andExpect(jsonPath("$[0].tasksCount").value(3))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_DELETED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_MEMBER_ADDED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_CREATED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_TASK_DELETED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_TASK_CREATED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_TASK_UPDATED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_ASSIGNED").value(true))
                    .andExpect(jsonPath("$[0].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(true))
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
        }
    }

    @Nested
    class ShouldNotAddMemberToProject {

        @Test
        void shouldNotAddMemberToProjectIfProjectDoesNotExist() throws Exception {
            // Given
            Long projectId = 100L;
            String memberToAddUsername = "jaroslawPsikuta";
            // When
            mockMvc.perform(get("/project/{id}", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
            // Then
            mockMvc.perform(post("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("usernames", memberToAddUsername))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotAddMemberToProjectIfMemberIsAlreadyAssignedToProject() throws Exception {
            // Given
            Long projectId = 2L;
            String username = "frneek";
            // When
            // Then
            mockMvc.perform(post("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("usernames", username))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("MEMBER_WITH_USERNAME_frneek_IS_ALREADY_ASSIGNED_TO_PROJECT_WITH_ID_2"))
                    .andDo(print());
        }

        @Test
        void shouldNotAddMemberToProjectWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 2L;
            String memberToAddUsername = "jaroslawPsikuta";
            // When
            // Then
            mockMvc.perform(post("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .param("usernames", memberToAddUsername))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotAddMemberToProjectWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 2L;
            String memberToAddUsername = "jaroslawPsikuta";
            // When
            // Then
            mockMvc.perform(post("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .param("usernames", memberToAddUsername))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldDiscardMemberFromProject {

        @Test
        void shouldDiscardMemberFromProject() throws Exception {
            // Given
            Long projectId = 1L;
            String memberToDiscardUsername = "jaroslawPsikuta";
            // When
            mockMvc.perform(get("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].username", containsInAnyOrder("frneek", "jaroslawPsikuta")))
                    .andExpect(jsonPath("$[*].projectCount", containsInAnyOrder(3, 1)))
                    .andExpect(jsonPath("$[*].commentsCount", containsInAnyOrder(3, 2)))
                    .andExpect(jsonPath("$[*].tasksCount", containsInAnyOrder(15, 3)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_DELETED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_REMOVED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_UPDATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_ASSIGNED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_CREATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_ASSIGNED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_DELETED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_CREATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_ADDED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("projects", "tasks", "task-comments", "join-requests", "join-responses", "statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/member/frneek/projects",
                            "http://localhost/member/frneek/tasks",
                            "http://localhost/member/frneek/comments",
                            "http://localhost/member/frneek/join/requests",
                            "http://localhost/member/frneek/join/responses",
                            "http://localhost/member/frneek/statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/member/jaroslawPsikuta/projects",
                            "http://localhost/member/jaroslawPsikuta/tasks",
                            "http://localhost/member/jaroslawPsikuta/comments",
                            "http://localhost/member/jaroslawPsikuta/join/requests",
                            "http://localhost/member/jaroslawPsikuta/join/responses",
                            "http://localhost/member/jaroslawPsikuta/statistics")))
                    .andDo(print());
            // Then
            mockMvc.perform(delete("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("username", memberToDiscardUsername))
                    .andExpect(status().isOk())
                    .andDo(print());

            mockMvc.perform(get("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[*].username", containsInAnyOrder("frneek")))
                    .andExpect(jsonPath("$[*].projectCount", containsInAnyOrder(3)))
                    .andExpect(jsonPath("$[*].commentsCount", containsInAnyOrder(3)))
                    .andExpect(jsonPath("$[*].tasksCount", containsInAnyOrder(15)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_DELETED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_REMOVED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_UPDATED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_ASSIGNED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_CREATED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_ASSIGNED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_DELETED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_CREATED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_ADDED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("projects", "tasks", "task-comments", "join-requests", "join-responses", "statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/member/frneek/projects",
                            "http://localhost/member/frneek/tasks",
                            "http://localhost/member/frneek/comments",
                            "http://localhost/member/frneek/join/requests",
                            "http://localhost/member/frneek/join/responses",
                            "http://localhost/member/frneek/statistics")))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotDiscardMemberFromProject {


        @Test
        void shouldNotDiscardMemberFromProjectIfProjectDoesNotExist() throws Exception {
            // Given
            Long nonExistingProjectId = 999L;
            String memberToDiscardUsername = "jaroslawPsikuta";
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
            mockMvc.perform(delete("/project/{id}/members", nonExistingProjectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("username", memberToDiscardUsername))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotDiscardMemberFromProjectIfMemberDoesNotExist() throws Exception {
            // Given
            Long projectId = 1L;
            String nonExistingMemberUsername = "nonExistingMember";
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
            mockMvc.perform(delete("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("username", nonExistingMemberUsername))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotDiscardMemberFromProjectIfMemberIsNotAssignedToProject() throws Exception {
            // Given
            Long projectId = 3L;
            String memberToDiscardUsername = "jaroslawPsikuta";
            // When
            mockMvc.perform(get("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[*].username", containsInAnyOrder("frneek")))
                    .andExpect(jsonPath("$[*].projectCount", containsInAnyOrder(3)))
                    .andExpect(jsonPath("$[*].commentsCount", containsInAnyOrder(3)))
                    .andExpect(jsonPath("$[*].tasksCount", containsInAnyOrder(15)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_DELETED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_REMOVED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_UPDATED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_ASSIGNED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_CREATED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_ASSIGNED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_DELETED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_CREATED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_ADDED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("projects", "tasks", "task-comments", "join-requests", "join-responses", "statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/member/frneek/projects",
                            "http://localhost/member/frneek/tasks",
                            "http://localhost/member/frneek/comments",
                            "http://localhost/member/frneek/join/requests",
                            "http://localhost/member/frneek/join/responses",
                            "http://localhost/member/frneek/statistics")))
                    .andDo(print());
            // Then
            mockMvc.perform(delete("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("username", memberToDiscardUsername))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("MEMBER_WITH_USERNAME_jaroslawPsikuta_IS_NOT_ASSIGNED_TO_PROJECT_WITH_ID_3"))
                    .andDo(print());
        }

        @Test
        void shouldNotDiscardMemberFromProjectWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            String memberToDiscardUsername = "jaroslawPsikuta";
            // When
            // Then
            mockMvc.perform(delete("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .param("username", memberToDiscardUsername))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotDiscardMemberFromProjectWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            String memberToDiscardUsername = "jaroslawPsikuta";
            // When
            // Then
            mockMvc.perform(delete("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .param("username", memberToDiscardUsername))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldAssignMembersToTask {

        @Test
        void shouldAssignMembersToTask() throws Exception {
            // Given
            Long projectId = 1L;
            Long taskId = 3L;
            String memberToAssignUsername = "jaroslawPsikuta";
            // When
            mockMvc.perform(get("/task/{id}/members", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(1))
                    .andExpect(jsonPath("$[*].username").value(hasItem("frneek")))
                    .andExpect(jsonPath("$[*].projectCount").value(hasItem(3)))
                    .andExpect(jsonPath("$[*].commentsCount").value(hasItem(3)))
                    .andExpect(jsonPath("$[*].tasksCount").value(hasItem(15)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_CREATED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_ASSIGNED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_DELETED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_UPDATED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_ADDED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_DELETED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_CREATED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].links[0].rel").value(hasItem("projects")))
                    .andExpect(jsonPath("$[*].links[0].href").value(hasItem("http://localhost/member/frneek/projects")))
                    .andExpect(jsonPath("$[*].links[1].rel").value(hasItem("tasks")))
                    .andExpect(jsonPath("$[*].links[1].href").value(hasItem("http://localhost/member/frneek/tasks")))
                    .andExpect(jsonPath("$[*].links[2].rel").value(hasItem("task-comments")))
                    .andExpect(jsonPath("$[*].links[2].href").value(hasItem("http://localhost/member/frneek/comments")))
                    .andExpect(jsonPath("$[*].links[3].rel").value(hasItem("join-requests")))
                    .andExpect(jsonPath("$[*].links[3].href").value(hasItem("http://localhost/member/frneek/join/requests")))
                    .andExpect(jsonPath("$[*].links[4].rel").value(hasItem("join-responses")))
                    .andExpect(jsonPath("$[*].links[4].href").value(hasItem("http://localhost/member/frneek/join/responses")))
                    .andExpect(jsonPath("$[*].links[5].rel").value(hasItem("statistics")))
                    .andExpect(jsonPath("$[*].links[5].href").value(hasItem("http://localhost/member/frneek/statistics")))
                    .andDo(print());

            // Then
            mockMvc.perform(post("/project/{projectId}/task/{taskId}/members", projectId, taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("usernames", memberToAssignUsername))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].username").value(hasItem(memberToAssignUsername)))
                    .andExpect(jsonPath("$[*].projectCount").value(hasItem(1)))
                    .andExpect(jsonPath("$[*].commentsCount").value(hasItem(2)))
                    .andExpect(jsonPath("$[*].tasksCount").value(hasItem(4)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_CREATED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_ASSIGNED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_UPDATED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_DELETED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_ADDED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_REMOVED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_DELETED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_ASSIGNED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED").value(hasItem(true)))
                    .andExpect(jsonPath("$[*].links[0].rel").value(hasItem("projects")))
                    .andExpect(jsonPath("$[*].links[0].href").value(hasItem("http://localhost/member/jaroslawPsikuta/projects")))
                    .andExpect(jsonPath("$[*].links[1].rel").value(hasItem("tasks")))
                    .andExpect(jsonPath("$[*].links[1].href").value(hasItem("http://localhost/member/jaroslawPsikuta/tasks")))
                    .andExpect(jsonPath("$[*].links[2].rel").value(hasItem("task-comments")))
                    .andExpect(jsonPath("$[*].links[2].href").value(hasItem("http://localhost/member/jaroslawPsikuta/comments")))
                    .andExpect(jsonPath("$[*].links[3].rel").value(hasItem("join-requests")))
                    .andExpect(jsonPath("$[*].links[3].href").value(hasItem("http://localhost/member/jaroslawPsikuta/join/requests")))
                    .andExpect(jsonPath("$[*].links[4].rel").value(hasItem("join-responses")))
                    .andExpect(jsonPath("$[*].links[4].href").value(hasItem("http://localhost/member/jaroslawPsikuta/join/responses")))
                    .andExpect(jsonPath("$[*].links[5].rel").value(hasItem("statistics")))
                    .andExpect(jsonPath("$[*].links[5].href").value(hasItem("http://localhost/member/jaroslawPsikuta/statistics")))
                    .andDo(print());

            mockMvc.perform(get("/task/{id}/members", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].username", containsInAnyOrder("frneek", "jaroslawPsikuta")))
                    .andExpect(jsonPath("$[*].projectCount", containsInAnyOrder(3, 1)))
                    .andExpect(jsonPath("$[*].commentsCount", containsInAnyOrder(3, 2)))
                    .andExpect(jsonPath("$[*].tasksCount", containsInAnyOrder(15, 4)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_DELETED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_REMOVED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_UPDATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_ASSIGNED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_CREATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_ASSIGNED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_DELETED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_CREATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_ADDED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("projects", "tasks", "task-comments", "join-requests", "join-responses", "statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/member/frneek/projects",
                            "http://localhost/member/frneek/tasks",
                            "http://localhost/member/frneek/comments",
                            "http://localhost/member/frneek/join/requests",
                            "http://localhost/member/frneek/join/responses",
                            "http://localhost/member/frneek/statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/member/jaroslawPsikuta/projects",
                            "http://localhost/member/jaroslawPsikuta/tasks",
                            "http://localhost/member/jaroslawPsikuta/comments",
                            "http://localhost/member/jaroslawPsikuta/join/requests",
                            "http://localhost/member/jaroslawPsikuta/join/responses",
                            "http://localhost/member/jaroslawPsikuta/statistics")))
                    .andDo(print());
        }
    }

    @Nested
    class ShouldNotAssignMembersToTask {


        @Test
        void shouldNotAssignMembersToTaskIfProjectDoesNotExist() throws Exception {
            // Given
            Long nonExistingProjectId = 999L;
            Long taskId = 3L;
            String memberToAssignUsername = "jaroslawPsikuta";
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
            mockMvc.perform(post("/project/{projectId}/task/{taskId}/members", nonExistingProjectId, taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("usernames", memberToAssignUsername))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("PROJECT_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotAssignMembersToTaskIfTaskDoesNotExist() throws Exception {
            // Given
            Long nonExistingTaskId = 999L;
            Long projectId = 3L;
            String memberToAssignUsername = "jaroslawPsikuta";
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
            mockMvc.perform(post("/project/{projectId}/task/{taskId}/members", projectId, nonExistingTaskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("usernames", memberToAssignUsername))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("TASK_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotAssignMembersToTaskIfMemberDoesNotExist() throws Exception {
            // Given
            Long projectId = 1L;
            Long taskId = 3L;
            String nonExistingMemberUsername = "nonExistingMember";
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
            mockMvc.perform(post("/project/{projectId}/task/{taskId}/members", projectId, taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("usernames", nonExistingMemberUsername))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(NOT_FOUND.value()))
                    .andExpect(jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("MEMBER_NOT_FOUND"))
                    .andDo(print());
        }

        @Test
        void shouldNotAssignMembersToTaskIfMemberIsNotAssignedToProject() throws Exception {
            // Given
            Long projectId = 2L;
            Long taskId = 9L;
            String memberToAssignUsername = "jaroslawPsikuta";
            // When
            mockMvc.perform(get("/project/{id}/members", projectId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[*].username", containsInAnyOrder("frneek")))
                    .andExpect(jsonPath("$[*].projectCount", containsInAnyOrder(3)))
                    .andExpect(jsonPath("$[*].commentsCount", containsInAnyOrder(3)))
                    .andExpect(jsonPath("$[*].tasksCount", containsInAnyOrder(15)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_DELETED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_REMOVED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_UPDATED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_ASSIGNED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_CREATED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_ASSIGNED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_DELETED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_CREATED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_ADDED", containsInAnyOrder(true)))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("projects", "tasks", "task-comments", "join-requests", "join-responses", "statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/member/frneek/projects",
                            "http://localhost/member/frneek/tasks",
                            "http://localhost/member/frneek/comments",
                            "http://localhost/member/frneek/join/requests",
                            "http://localhost/member/frneek/join/responses",
                            "http://localhost/member/frneek/statistics")))
                    .andDo(print());
            // Then
            mockMvc.perform(post("/project/{projectId}/task/{taskId}/members", projectId, taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("usernames", memberToAssignUsername))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("MEMBER_WITH_USERNAME_jaroslawPsikuta_IS_NOT_ASSIGNED_TO_PROJECT_WITH_ID_2"))
                    .andDo(print());
        }

        @Test
        void shouldNotAssignMembersToTaskIfMemberIsAlreadyAssignedToTask() throws Exception {
            // Given
            Long projectId = 1L;
            Long taskId = 1L;
            String memberUsername = "jaroslawPsikuta";
            // When
            mockMvc.perform(get("/task/{id}/members", taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].username", containsInAnyOrder("frneek", "jaroslawPsikuta")))
                    .andExpect(jsonPath("$[*].projectCount", containsInAnyOrder(3, 1)))
                    .andExpect(jsonPath("$[*].commentsCount", containsInAnyOrder(3, 2)))
                    .andExpect(jsonPath("$[*].tasksCount", containsInAnyOrder(15, 3)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_DELETED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_REMOVED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_REJECTED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_UPDATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_CREATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_JOIN_REQUEST_ACCEPTED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_ASSIGNED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_CREATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_ASSIGNED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_DELETED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_TASK_CREATED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].notificationSubscriptions.PROJECT_MEMBER_ADDED", containsInAnyOrder(true, true)))
                    .andExpect(jsonPath("$[*].links[*].rel").value(hasItems("projects", "tasks", "task-comments", "join-requests", "join-responses", "statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/member/frneek/projects",
                            "http://localhost/member/frneek/tasks",
                            "http://localhost/member/frneek/comments",
                            "http://localhost/member/frneek/join/requests",
                            "http://localhost/member/frneek/join/responses",
                            "http://localhost/member/frneek/statistics")))
                    .andExpect(jsonPath("$[*].links[*].href").value(hasItems(
                            "http://localhost/member/jaroslawPsikuta/projects",
                            "http://localhost/member/jaroslawPsikuta/tasks",
                            "http://localhost/member/jaroslawPsikuta/comments",
                            "http://localhost/member/jaroslawPsikuta/join/requests",
                            "http://localhost/member/jaroslawPsikuta/join/responses",
                            "http://localhost/member/jaroslawPsikuta/statistics")))
                    .andDo(print());
            // Then
            mockMvc.perform(post("/project/{projectId}/task/{taskId}/members", projectId, taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .header(USERNAME, "frneek")
                            .param("usernames", memberUsername))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("MEMBER_WITH_USERNAME_jaroslawPsikuta_IS_ALREADY_ASSIGNED_TO_TASK_WITH_ID_1"))
                    .andDo(print());
        }

        @Test
        void shouldNotAssignMembersToTaskWithoutRoleHeader() throws Exception {
            // Given
            Long projectId = 1L;
            Long taskId = 3L;
            String memberToAssignUsername = "jaroslawPsikuta";
            // When
            // Then
            mockMvc.perform(post("/project/{projectId}/task/{taskId}/members", projectId, taskId)
                            .contentType(APPLICATION_JSON)
                            .header(USERNAME, "frneek")
                            .param("usernames", memberToAssignUsername))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("ROLE_HEADER_IS_MISSING"))
                    .andDo(print());
        }

        @Test
        void shouldNotAssignMembersToTaskWithoutUsernameHeader() throws Exception {
            // Given
            Long projectId = 1L;
            Long taskId = 3L;
            String memberToAssignUsername = "jaroslawPsikuta";
            // When
            // Then
            mockMvc.perform(post("/project/{projectId}/task/{taskId}/members", projectId, taskId)
                            .contentType(APPLICATION_JSON)
                            .header(ROLE, "ADMIN")
                            .param("usernames", memberToAssignUsername))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.httpStatusCode").value(BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.httpStatus").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("USERNAME_HEADER_IS_MISSING"))
                    .andDo(print());
        }
    }
}